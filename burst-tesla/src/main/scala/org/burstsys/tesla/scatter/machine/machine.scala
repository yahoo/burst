/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import org.burstsys.tesla.configuration.scatterTenderPeriod
import org.burstsys.vitals.background.{VitalsBackgroundFunction, VitalsBackgroundFunctions}
import org.burstsys.vitals.logging._

package object machine extends VitalsLogger {

  lazy val backgroundTender: VitalsBackgroundFunctions =
    new VitalsBackgroundFunctions(s"fab-scatter-tender", scatterTenderPeriod, scatterTenderPeriod).start

  /**
   * operations to get a scatter started and to finalize after its done
   */
  trait TeslaScatterLifecycle extends Any {

    /**
     * start the '''scatter''' i.e. start all '''scatter slots''' requests
     */
    def execute(): Unit

  }

  /**
   * operations that change the state of the scatter from active to terminated
   */
  trait TeslaScatterTerminator extends Any {

    /**
     * For some reason we want to cancel this scatter. We need to cancel
     * all remaining active '''scatter slots''' and then cancel the scatter itself
     *
     */
    def scatterCancel(message: String): Unit

    /**
     * We have determined that this scatter can be considered a ''succcess''.
     * All currently active slots are cancelled and all current non responsive
     * requests become zombies.
     */
    def scatterSucceed(): Unit

    /**
     * We have determined that this scatter can be considered a ''failure'' and has some
     * sort of exception.
     * All currently active slots are cancelled and all current non responsive
     * requests become zombies.
     *
     * @param throwable
     * @return
     */
    def scatterFail(throwable: Throwable): Unit

    /**
     *
     * @param message
     */
    def scatterTimeout(message: String): Unit

  }

}
