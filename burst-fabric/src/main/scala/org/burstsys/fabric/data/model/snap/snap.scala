/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model

import org.burstsys.fabric.configuration.burstFabricCacheSpindleFoldersProperty
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ThreadLocalRandom
import scala.jdk.CollectionConverters._
import scala.concurrent.duration._
import scala.language.postfixOps


package object snap extends VitalsLogger {

  final val acquireWriteLockTimeout = 20 seconds

  final val snapSuffix = ".snap"

  final val tmpSuffix = ".snap_tmp"

  trait FabricSnapComponent extends Any {
    /**
     *
     * @return
     */
    def snap: FabricSnap

    def wireSnap(snap: FabricSnap): Unit

  }

  /**
   * the current state of the SNAP
   *
   * @param code
   */
  sealed case class FabricSnapState(code: Int) {
    override def toString: String = getClass.getSimpleName.stripSuffix("$")
  }

  /**
   * slice is not in local disk cache
   */
  object ColdSnap extends FabricSnapState(1)

  /**
   * slice is in local disk cache but not in memory
   */
  object WarmSnap extends FabricSnapState(2)

  /**
   * slice is in memory (and in local disk cache)
   */
  object HotSnap extends FabricSnapState(3)


  /**
   * slice is known, but had a failure
   */
  object FailedSnap extends FabricSnapState(4)

  /**
   * slice did a cold load that resulted in all runt (empty) regions...
   * these are mostly treated as [[HotSnap]] but they can't/won't be
   * evicted or flushed
   */
  object NoDataSnap extends FabricSnapState(6)

  /**
   * get any and all persisted snap files from persistent store (local disk)
   *
   * @return
   */
  final
  def getPersistedSnapFiles: Array[Path] = {
    // delete any tmp files
    snapFolders foreach {
      folder =>
        val stream = Files.newDirectoryStream(Paths.get(folder), TmpSnapFileFilter)
        try {
          stream.forEach { file => Files.deleteIfExists(file) }
        } finally stream.close()
    }
    // load any normal files
    snapFolders flatMap {
      folder =>
        val stream = Files.newDirectoryStream(Paths.get(folder), SnapFileFilter)
        try {
          stream.asScala map { file => Paths.get(file.toString.stripSuffix(snapSuffix)) }
        } finally stream.close()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // snap folders/files...
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the set of folders (spindles) available
   */
  final
  lazy val snapSpindleFolders: Array[String] = {
    // TODO unify this with reqion cache eventually
    burstFabricCacheSpindleFoldersProperty.getOrThrow split ";"
  }

  /**
   * the set of snap subfolders
   */
  final
  lazy val snapFolders: Array[String] = {
    snapSpindleFolders map {
      folder =>
        try {
          val snapFolder = Paths get(folder, "snap")
          if (!(Files exists snapFolder))
            Files createDirectory snapFolder
          snapFolder.toString
        } catch safely {
          case t: Throwable =>
            log error burstStdMsg(t)
            throw t
        }
    }
  }

  final
  object SnapFileFilter extends DirectoryStream.Filter[Path]() {
    def accept(entry: Path): Boolean = entry.toAbsolutePath.toString.endsWith(snapSuffix)
  }

  final
  object TmpSnapFileFilter extends DirectoryStream.Filter[Path]() {
    def accept(entry: Path): Boolean = entry.toAbsolutePath.toString.endsWith(tmpSuffix)
  }

  /**
   * get a random snap folder in order to distribute evenly across spindles
   *
   * @return
   */
  final
  def getSnapFolder: Path = {
    val index = Math.abs(ThreadLocalRandom.current.nextInt() % snapFolders.length)
    try {
      val folder = snapFolders(index)
      Paths get folder
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  /**
   * get a new snap file, round robin'ed through the spindles. This is somewhat to
   * provide  performance, but mostly to give us the ability to eventually handle the
   * loss of a single spindle in some cases.
   *
   * @param slice
   * @return
   */
  final
  def getSnapFile(slice: FabricSlice): Path = {
    val folder = getSnapFolder
    val fileName = s"snap_${slice.guid}_${slice.sliceKey}"
    Paths.get(folder.toString, fileName)
  }

}
