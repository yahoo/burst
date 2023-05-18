/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.snap

import java.io.FileOutputStream
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Path, Paths}

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.configuration._
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.model.slice.state.{FabricDataCold, FabricDataFailed, FabricDataHot, FabricDataNoData, FabricDataWarm}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * The local file system persistence state and functions of the snap
 * A snap consists of a ''snap'' metadata file (header)
 * and some number of ''region'' data files
 * one for each core involved.
 */
trait FabricSnapImage extends KryoSerializable {

  self: FabricSnapContext =>

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  final override def snapFile: Path = Paths.get(path.toString + snapSuffix)

  ///////////////////////////////////////////////////////////////////
  // private state
  ///////////////////////////////////////////////////////////////////

  private[this]
  var _path: Path = _

  private[this]
  var _metadata: FabricSliceMetadata = _

  private[this]
  var _data: FabricSliceData = _

  private[this]
  var _evictTtlMs: Long = 0

  private[this]
  var _flushTtlMs: Long = 0

  private[this]
  var _eraseTtlMs: Long = 0

  private[this]
  var _state: FabricSnapState = ColdSnap

  private[this]
  var _lastAccessTime: Long = System.currentTimeMillis()

  private[this]
  var _totalAccessCount: Long = 0

  private[this]
  var _healingFault = VitalsHealingFault(burstViewCacheFaultHealProperty.get)

  ///////////////////////////////////////////////////////////////////
  // accessors
  ///////////////////////////////////////////////////////////////////

  final override def metadata: FabricSliceMetadata = _metadata

  final override def data: FabricSliceData = _data

  final override def lastAccessTime: Long = _lastAccessTime

  final override def totalAccessCount: Long = _totalAccessCount

  final override
  def recordAccess: FabricSnap = {
    synchronized {
      _lastAccessTime = System.currentTimeMillis()
      _totalAccessCount += 1
    }
    this
  }

  final override def state: FabricSnapState = _state

  final override def state_=(s: FabricSnapState): Unit = synchronized {
    _state = s
    // TODO at some point get rid of the metadata state distinction
    // update the 'other' state as well
    s match {
      case ColdSnap => metadata.state = FabricDataCold
      case WarmSnap => metadata.state = FabricDataWarm
      case HotSnap => metadata.state = FabricDataHot
      case NoDataSnap => metadata.state = FabricDataNoData
      case FailedSnap => metadata.state = FabricDataFailed
      case s => log warn s"SNAP_IMAGE_UNKNOWN_STATE $s"
    }
    notifyAll()
  }

  override def waitState(ms: Long): Unit = synchronized(wait(ms))

  final override def failCount: Int = _healingFault.faultCount

  final override def lastFail_=(t: Throwable): Unit = {
    log error s"FabricSnapImage.lastFailure(${t.getMessage})"
    _healingFault.recordFault(t)
    state = FailedSnap
  }

  final override def resetLastFail(): Unit = _healingFault.heal()

  final override def lastFail: Option[Throwable] = _healingFault.fault

  final override def evictTtlMs: Long = _evictTtlMs

  final override def flushTtlMs: Long = _flushTtlMs

  final override def eraseTtlMs: Long = _eraseTtlMs

  final override def guid: VitalsUid = slice.guid

  final def path: Path = _path

  ///////////////////////////////////////////////////////////////////
  // file ops
  ///////////////////////////////////////////////////////////////////

  final
  def open(path: Path): FabricSnap = {
    lazy val tag = s"FabricSnapImage.open($parameters)"
    _path = path
    _metadata = FabricSliceMetadata(this)
    _data = FabricSliceData(this)
    _evictTtlMs = evictTtlMsFromDatasource(slice.datasource)
    _flushTtlMs = flushTtlMsFromDatasource(slice.datasource)
    _eraseTtlMs = eraseTtlMsFromDatasource(slice.datasource)
    log debug burstStdMsg(s"SNAP_OPEN $tag")
    this
  }

  final override
  def delete : FabricSnap = {
    lazy val tag = s"FabricSnapImage.delete($parameters)"
    try {
      Files.delete(Paths.get(path.toString + snapSuffix))
      this
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"SNAP_DELETE_FAIL $t $tag", t)
        throw t
    }
  }

  /**
   * persist the current state of this snap to disk
   *
   * @return
   */
  final override
  def persist: FabricSnap = {
    lazy val tag = s"FabricSnapImage.persist($parameters)"
    try {
      val tmpFile = createFileDeleteIfExists(path, tmpSuffix)

      // serialize into the file
      val kryo = org.burstsys.vitals.kryo.acquireKryo
      val output = new Output(new FileOutputStream(tmpFile.toFile))
      kryo.writeClassAndObject(output, this)
      output.close()

      // atomic all or nothing creation of final snap file
      val finalFile = createFileDeleteIfExists(path, snapSuffix)
      Files.move(tmpFile, finalFile, ATOMIC_MOVE)

      log debug s"SNAP_PERSIST $tag"
      this

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        throw t
    }
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  final override
  def read(kryo: Kryo, input: Input): Unit = {
    _path = Paths.get(input.readString)
    _state = kryo.readClassAndObject(input).asInstanceOf[FabricSnapState]
    slice = kryo.readClassAndObject(input).asInstanceOf[FabricSlice]
    _metadata = kryo.readClassAndObject(input).asInstanceOf[FabricSliceMetadata]
    _data = kryo.readClassAndObject(input).asInstanceOf[FabricSliceData]
    _healingFault = kryo.readClassAndObject(input).asInstanceOf[VitalsHealingFault]
    _evictTtlMs = input.readLong
    _flushTtlMs = input.readLong
    _lastAccessTime = input.readLong
    _totalAccessCount = input.readLong
    _data.wireSnap(this) // wire in snap pointers for complex object
  }

  final override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(_path.toString)
    kryo.writeClassAndObject(output, _state)
    kryo.writeClassAndObject(output, slice)
    kryo.writeClassAndObject(output, _metadata)
    kryo.writeClassAndObject(output, _data)
    kryo.writeClassAndObject(output, _healingFault)
    output writeLong _evictTtlMs
    output writeLong _flushTtlMs
    output writeLong _lastAccessTime
    output writeLong _totalAccessCount
  }

  ///////////////////////////////////////////////////////////////////
  // internals
  ///////////////////////////////////////////////////////////////////

  private
  def createFileDeleteIfExists(path: Path, suffix: String): Path = {
    // delete if exists
    val fullPath = Paths.get(path.toString + suffix)
    Files.deleteIfExists(fullPath)

    // create the file and make sure it has the correct permissions
    val perms = PosixFilePermissions.fromString("rw-r--r--")
    val fileAttrs = PosixFilePermissions.asFileAttribute(perms)
    Files.createFile(fullPath, fileAttrs)
  }

}
