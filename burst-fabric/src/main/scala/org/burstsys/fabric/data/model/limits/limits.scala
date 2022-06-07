/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model

import java.nio.file.Paths
import org.burstsys.fabric.data.model.snap.snapFolders
import org.burstsys.tesla.offheap
import org.burstsys.vitals.host
import org.burstsys.vitals.instrument.prettyByteSizeString
import org.burstsys.vitals.logging._

package object limits extends VitalsLogger {

  /**
   * return a floating point number between 0.0 and 100.0 that represents the
   * amount of memory in use (this is currently for the 'os' or entire node but should
   * be for the JVM process)
   *
   * @return
   */
  def memoryPercentUsed: Double = percentUsed("memory", offheap.nativeMemoryMax, used = host.mappedMemoryUsed)

  /**
   * return a floating point number between 0.0 and 100.0 that represents the
   * amount of snap folder allocated disk in use (this is currently for the 'os' or entire node but should
   * be for the JVM process). If there are multiple folders/file systems in use then this is the percent
   * used in the ''most used'' instance.
   *
   * @return
   */
  def diskPercentUsed: Double = {
    (snapFolders map {
      folder =>
        val file = Paths.get(folder).toFile
        percentUsed("disk", file.getTotalSpace, usable = file.getUsableSpace)
    }).max
  }

  private
  def percentUsed(name: String, total: Long, usable: Long = -1, used: Long = -1): Double = {
    val free = if (usable > -1) usable else total - used
    val percentUsed = ((total - free).toDouble / total.toDouble) * 100.0
    log debug burstStdMsg(f"resource '$name' percentUsed=$percentUsed%.2f (free=${
      prettyByteSizeString(free)
    }, total=${
      prettyByteSizeString(total)
    })")
    percentUsed
  }

}
