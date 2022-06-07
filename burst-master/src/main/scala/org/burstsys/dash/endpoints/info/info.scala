/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints

import org.burstsys.dash.configuration
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyTimeFromMillis}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.{git, host}
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}

package object info extends VitalsLogger {

  final case
  class BurstBuildInfoJson(
                            branch: String = git.branch.toUpperCase,
                            build: String = git.buildVersion,
                            commitId: String = git.commitId.toUpperCase
                          ) extends VitalsJsonObject

  final case
  class BurstHostInfoJson(
                           hostName: String = getPublicHostName,
                           hostAddress: String = getPublicHostAddress,
                           restPort: Int = configuration.burstRestPortProperty.getOrThrow,
                           osName: String = host.osName,
                           osArchitecture: String = host.osArch,
                           osVersion: String = host.osVersion,
                           loadAverage: String = f"${host.loadAverage}%.1f",
                           uptime: String = prettyTimeFromMillis(host.uptime),
                           cores: String = host.cores,
                           currentThreads: String = host.threadsCurrent.toString,
                           peakThreads: String = host.threadsPeak.toString,
                           usedHeap: String = prettyByteSizeString(host.heapUsed),
                           committedHeap: String = prettyByteSizeString(host.heapCommitted),
                           maxHeap: String = prettyByteSizeString(host.heapMax),
                           gc: String = host.gcReadout
                         ) extends VitalsJsonObject

  final case
  class BurstSettingInfoJson(
                              value: Any,
                              dataType: String,
                              description: String
                            ) extends VitalsJsonObject

}
