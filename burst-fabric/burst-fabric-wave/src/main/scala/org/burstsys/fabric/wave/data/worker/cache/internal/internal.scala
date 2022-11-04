/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache

import org.apache.commons.io.FileUtils
import org.burstsys.fabric.wave.data.model.slice.region.DeleteFileFilter
import org.burstsys.fabric.wave.data.model.slice.region.regionFolders
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import java.nio.file.Files
import scala.concurrent.duration._
import scala.language.postfixOps

package object internal extends VitalsLogger {

  /**
   * you should never have to wait very long to get a global cache lock
   */
  final val cacheLockAcquireTimeout = 30 seconds

  final
  lazy val backgroundCleaner = new VitalsBackgroundFunction(
    "fab-region-cache-boot-cleaner", 5 minutes, 10 minutes, {
      log info s"scanning for boot deleted region files to be cleaned"
      regionFolders foreach {
        folder =>
          try {
            val stream = Files newDirectoryStream(folder, DeleteFileFilter)
            try {
              stream forEach {
                path =>
                  FileUtils.deleteQuietly(path.toFile)
                  log info s"deleted region file $path"
              }
            } finally stream.close()
          } catch safely {
            case t: Throwable => log error burstStdMsg(t)
          }
      }
    }
  )

}
