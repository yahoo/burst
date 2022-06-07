/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

package object trek extends VitalsLogger {

  lazy val trekEnable: Boolean = configuration.vitalsEnableTrekProperty.getOrThrow

  case class VitalsTrekCluster(name: String) {
    override def toString: VitalsUid = name
  }

  object VitalsTrekCell extends VitalsTrekCluster("cell")

  object VitalsTrekRemote extends VitalsTrekCluster("remote")

  case class VitalsTrekRole(name: String) {
    override def toString: VitalsUid = name
  }

  object VitalsTrekMaster extends VitalsTrekRole("master")

  object VitalsTrekWorker extends VitalsTrekRole("worker")

  case
  class VitalsTrekMark(
                        tmark: String,
                        cluster: VitalsTrekCluster,
                        role: VitalsTrekRole
                      ) {

    final
    def begin(tuid: VitalsUid): Unit = {
      if (trekEnable)
        log info s"----> VITALS_TREK( tuid=$tuid, tmark=${tmark}_begin, tcluster=$cluster, trole=$role, t_host=${net.getPublicHostName}, t_thread=${Thread.currentThread.getName} )"
    }

    final
    def end(tuid: VitalsUid): Unit = {
      if (trekEnable)
        log info s"----> VITALS_TREK( tuid=$tuid, tmark=${tmark}_end, tcluster=$cluster, trole=$role, t_host=${net.getPublicHostName}, t_thread=${Thread.currentThread.getName} )"
    }

    final
    def fail(tuid: VitalsUid): Unit = {
      if (trekEnable)
        log info s"----> VITALS_TREK( tuid=$tuid, tmark=${tmark}_fail, tcluster=$cluster, trole=$role, t_host=${net.getPublicHostName}, t_thread=${Thread.currentThread.getName} )"
    }

  }

}
