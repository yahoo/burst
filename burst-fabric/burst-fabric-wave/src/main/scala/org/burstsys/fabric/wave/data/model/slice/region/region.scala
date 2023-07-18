/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice

import org.apache.commons.io.FileUtils
import org.burstsys.fabric
import org.burstsys.fabric.wave.configuration.cacheSpindleFolders
import org.burstsys.tesla.TeslaTypes.SizeOfByte
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.nio.ByteBuffer
import java.nio.file.DirectoryStream
import java.nio.file.FileSystems
import java.nio.file.Path
import scala.collection.mutable.ArrayBuffer

/**
 * Regions are a worker local concept that divides up a dataset into a set of CPU thread isolated/bound 'regions'
 */
package object region extends VitalsLogger  {

  type FabricRegionTag = String

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // region file header
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * region file Header encoding version
   */
  final
  val RegionVersion: Byte = 0x1.toByte

  final
  val SizeOfRegionVersion = SizeOfByte

  /**
   * region file header 'magic' value
   */
  final
  val RegionMagic: Byte = 85.toByte

  final
  val SizeOfRegionMagic = SizeOfByte

  final
  val SizeOfRegionHeader = SizeOfRegionVersion + SizeOfRegionMagic

  /**
   * the region file header as a byte buffer that can be re-used.
   */
  final
  val RegionHeaderData = ByteBuffer.wrap(Array(RegionMagic, RegionVersion))

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // region file/folder structure
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val regionFileSuffix = ".region"

  final
  object RegionFileFilter extends DirectoryStream.Filter[Path]() {
    def accept(entry: Path): Boolean = entry.toAbsolutePath.toString.endsWith(regionFileSuffix)
  }

  final val deleteFileSuffix = ".delete"

  final
  object DeleteFileFilter extends DirectoryStream.Filter[Path]() {
    def accept(entry: Path): Boolean = entry.toAbsolutePath.toString.endsWith(deleteFileSuffix)
  }

  final val regionFolderName = "regions"

  /**
   * We want to take the available folders and the number of regions specified and
   * distribute them among the folder(s). This will end up with each spindle have the region folder
   * for a subset of the cpu threads. There will be a region folder for each thread. You cannot have more
   * than the worker thread count
   */
  lazy val regionFolders: Array[Path] = {
    try {
      val folderPaths = new ArrayBuffer[Path]
      val spindles = cacheSpindleFolders
      val regionCount = fabric.wave.configuration.burstFabricCacheRegionCount

      var spindleIndex = 0
      for (i <- 0 until regionCount) {
        val folderPath = FileSystems.getDefault.getPath(spindles(spindleIndex), regionFolderName, f"$i%02d")
        FileUtils.forceMkdir(folderPath.toAbsolutePath.toFile)
        folderPaths += folderPath
        spindleIndex = (spindleIndex + 1) % spindles.length
      }

      log info
        s"""CACHE_REGION_FOLDER_CREATE regionCount=$regionCount, spindleCount=${spindles.length}
           |${folderPaths.mkString("\t'", "',\n\t'", "'")}""".stripMargin
      folderPaths.toArray
    } catch safely {
      case t: Throwable =>
        log error(burstStdMsg(t), t)
        throw t
    }
  }


}
