/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.data

import java.nio.file.{FileSystems, Path, Paths}

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.data.model.slice.region.{FabricRegion, regionFolders}
import org.burstsys.fabric.data.model.slice.sliceToFilePath
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * management of all persistent state for slice
 */
trait FabricSliceImage extends AnyRef with KryoSerializable {

  self: FabricSliceDataContext =>

  final override def toString: String =
    s"FabricSliceData($parameters, regionCount=${regionCount}, regions=${_regionList.map(_.filePath).mkString("\n\t'", "'\n\t'", "'")})"

  //////////////////////////////////////////
  // private state
  //////////////////////////////////////////

  /**
   * The list of paths where the regions files live
   */
  private[this]
  var _regionPaths: Array[Path] = Array.empty

  /**
   * This is the set of regions containing the data for the slice.
   */
  private[this]
  var _regionList: ArrayBuffer[FabricRegion] = ArrayBuffer.empty

  /**
   * The first region is randomized ordinal for round robin - don't always start at first region
   */
  private[this]
  var _nextRegion: Int = math.abs(Random.nextInt()) % regionFolders.length

  private[this]
  var _sliceOnDisk = false

  private[this]
  var _sliceInMemory = false

  //////////////////////////////////////////
  // API
  //////////////////////////////////////////

  final override
  def wireSnap(s: FabricSnap): Unit = {
    snap = s
    _regionList.foreach(_.wireSnap(s))
  }

  final protected
  def regions: Array[FabricRegion] = _regionList.toArray

  final override
  def regionCount: Int = _regionList.length

  final protected
  def -=(r: FabricRegion): Unit = _regionList -= r

  final override
  def sliceInMemory: Boolean = _sliceInMemory

  final def sliceInMemory_=(s: Boolean): Unit = _sliceInMemory = s

  final override
  def sliceOnDisk: Boolean = _sliceOnDisk

  final def sliceOnDisk_=(s: Boolean): Unit = _sliceOnDisk = s

  final protected
  def nextRegion: FabricRegion = {
    _nextRegion = (_nextRegion + 1) % regionCount
    _regionList(_nextRegion)
  }

  //////////////////////////////////////////
  // INTERNAL
  //////////////////////////////////////////

  /**
   * Creates a [[FabricRegion]] for each path in _regionPaths. If we are restoring a slice from disk
   * _regionPaths contains only non-empty regions. For a new slice _regionPaths contains a path in
   * each region folder.
   */
  final def initializeRegions(): Unit = {
    if (_regionPaths.isEmpty) {
      _regionPaths = regionFolders.map {
        p => FileSystems.getDefault.getPath(p.toAbsolutePath.toString, sliceToFilePath(snap.metadata))
      }
    }

    _regionList = new ArrayBuffer[FabricRegion]
    var regionIndex = 0
    do {
      _regionList += FabricRegion(snap, regionIndex, _regionPaths(regionIndex))
      regionIndex += 1
    } while(regionIndex < _regionPaths.length)
  }

  //////////////////////////////////////////
  // KRYO Serialization
  //////////////////////////////////////////

  final override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeInt(magicNumber)
    output.writeInt(version)
    output.writeInt(_nextRegion)
    output.writeInt(_regionPaths.length)
    regions.foreach(r => output.writeString(r.filePath.toFile.getAbsolutePath))
  }

  final override
  def read(kryo: Kryo, input: Input): Unit = {
    if(input.readInt != magicNumber)
      throw VitalsException(s"SLICE_DATA_BAD_MAGIC ${parameters}")
    if(input.readInt != version)
      throw VitalsException(s"SLICE_DATA_BAD_VERSION ${parameters}")
    _nextRegion = input.readInt()
    val regionCount = input.readInt()
    _regionPaths = (for (_ <- 0 until regionCount) yield Paths.get(input.readString())).toArray
    initializeRegions()
  }

}
