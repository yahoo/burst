/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache.lifecycle

import org.burstsys.fabric.configuration.burstFabricCacheBootFlushProperty
import org.burstsys.fabric.data.worker.cache.{FabricSnapCache, FabricSnapCacheContext}
import org.burstsys.fabric.data.worker.cache.internal.{FabricSnapCacheLocks, FabricSnapCacheMap}
import org.burstsys.fabric.data.model.snap._
import org.burstsys.vitals.errors._
import org.apache.commons.io.FileUtils

import scala.language.postfixOps
import org.burstsys.vitals.logging._

/**
 * [[FabricSnapCache]] JVM boot time lifecycle functions
 * If `burstFabricCacheBootFlushProperty` is true, then delete the snap files instead or reloading
 * them at boot.
 */
trait FabricSnapCacheBooter extends AnyRef  {

  self: FabricSnapCacheContext =>

  private
  def printFolders: String = snapFolders.mkString("\n\t", ", \n\t", "")

  final protected
  def bootSnapCache(): Unit = {
    lazy val tag = s"FabricSnapCacheBooter.bootSnapCache"
    var loadCount = 0
    var deleteCount = 0
    try {
      acquireGlobalCacheLock()
      try {
        getPersistedSnapFiles foreach {
          file =>
            if (burstFabricCacheBootFlushProperty.getOrThrow) {
              FileUtils.deleteQuietly(file.toFile)
              deleteCount += 1
            } else {
              val snap = FabricSnap(file)
              snap state = ColdSnap
              this += snap
              loadCount += 1
            }
        }
      } finally releaseGlobalCacheLock()
      if (loadCount > 0)
        log info f"CACHE_SNAP_LOADED loadCount=$loadCount%,d FROM ${printFolders} $tag"
      if (deleteCount > 0)
        log info f"CACHE_SNAP_DELETED deleteCount=$deleteCount%,d FROM ${printFolders} $tag"
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"CACHE_FAIL $t $tag", t)
        throw t
    }
  }

}
