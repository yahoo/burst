/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache.internal

import org.burstsys.fabric.wave.configuration.burstFabricCacheBootFlushProperty
import org.burstsys.fabric.wave.data.model.slice.region.RegionFileFilter
import org.burstsys.fabric.wave.data.model.slice.region.deleteFileSuffix
import org.burstsys.fabric.wave.data.model.slice.region.regionFileSuffix
import org.burstsys.fabric.wave.data.model.slice.region.regionFolders
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.nio.file.Files
import java.nio.file.Paths
import scala.language.postfixOps

/**
 * Management of [[org.burstsys.fabric.data.model.slice.region.FabricRegion]] objects in [[FabricSnapCache]]
  */
trait FabricSnapCacheRegions extends Any {

  /**
   * initialize the cache - loading slices and their regions as they are found. Flush if enabled...
   */
  final
  def initializeRegionFiles(): Unit = {
    lazy val tag = s"FabricSnapCacheRegions.initializeRegionFiles"
    try {
      if (burstFabricCacheBootFlushProperty.getOrThrow)
        cleanAllRegionFiles()
      else
        loadRegionFiles()
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(s"CACHE_FAIL $t $tag", t)
        log error msg
        throw VitalsException(msg, t)
    }
  }

  /**
   * clean up all cached storage of region files
   */
  final private
  def cleanAllRegionFiles(): Unit = {
    lazy val tag = s"FabricSnapCacheRegions.cleanAllRegionFiles"
    log info s"CACHE_DELETE_ALL_REGION_FILES $tag "
    regionFolders foreach {
      folder =>
        try {
          val stream = Files.newDirectoryStream(folder, RegionFileFilter)
          try {
            stream forEach {
              path =>
                var i = 1
                val fileName = path.toString.stripSuffix(regionFileSuffix)
                var deletedFile = Paths.get(fileName + deleteFileSuffix)
                try {
                  // in unit tests we run into odd situations where we restart before we have deleted the files.
                  while (Files.exists(deletedFile)) {
                    deletedFile = Paths.get(s"${fileName}_$i$deleteFileSuffix")
                    i += 1
                  }
                  Files.move(path, deletedFile)
                } catch safely {
                  case t: Throwable =>
                    log error burstStdMsg(t)
                }
            }
          } finally stream.close()
        } catch safely {
          case t: Throwable =>
            val msg = burstStdMsg(s"CACHE_FAIL $t $tag", t)
            log error msg
            throw VitalsException(msg, t)
        }
    }
  }

  /**
   * Scan through the region files we find and try to create slices from them, Afterwards we check
   * for stale regions and delete the associated slice and all its regions.
   */
  final private
  def loadRegionFiles(): Unit = {
    lazy val tag = s"FabricSnapCacheRegions.loadRegionFiles"
    throw VitalsException(s"$tag CURRENTLY WE DO NOT SUPPORT LOADING OF SNAP/REGION FILES AT BOOT...")
    /*
        try {
          log debug s"$hdr discovering disc resident region files..."
          log debug s"$hdr initializing... looking in folders ${
            cacheSpindleFolders.mkString("{ ", ", ", " }")
          }"
          regionFolders foreach {
            folder =>
              val stream = Files newDirectoryStream(folder, RegionFileFilter)
              try {
                stream foreach {
                  path =>
                    pathToSlice(path) match {
                      case null => Files delete path
                      case p => descriptorCache.getOrElseUpdate(
                        p, FabricRegionCacheDescriptor(this, p)
                      ).sliceData addDiscoveredRegionPath path
                    }
                }
              } finally stream.close()
          }
          log debug s"$hdr discovered ${descriptorCache.values.size} disc resident region descriptor(s)"

          log debug s"$hdr checking for truncated regions..."
          descriptorCache.values foreach {
            descriptor =>
              var zeroSizeFile = false
              descriptor.sliceData.regionPaths.foreach(s => if (Files.size(s) <= 0) zeroSizeFile = true)
              if (zeroSizeFile) {
                log warn s"$hdr $descriptor has at least one runt region - deleting it's region file(s)"
                descriptor.sliceData.deleteAllRegionFiles
                flushDescriptor(descriptor)
              }
          }

          // state is no longer unknown - its on disc
          descriptorCache.values foreach (_.metadata.state = FabricDataWarm)

          log debug s"$hdr ${descriptorCache.size} validated slice descriptor(s) in cache"
        } catch safely {
          case t: Throwable =>
            val msg = burstStdMsg(t)
            log error msg
            throw VitalsException(msg)
        }
    */

  }


}
