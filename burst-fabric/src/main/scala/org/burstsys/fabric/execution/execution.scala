/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.exception.FabricException
import org.burstsys.vitals.logging._

package object execution extends VitalsLogger {

  /**
   * type that has releasable resources on either the
   * worker or master sides
   */
  trait FabricResourceHolder extends Any {

    /**
     * release resources on the master side
     */
    def releaseResourcesOnMaster(): Unit = {}

    /**
     * release resources on the worker side
     */
    def releaseResourcesOnWorker(): Unit = {}

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DEPRECATED
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a [[FabricException]] exception originating from an '''execution''' aspect of fabric pipelines
   *
   * @deprecated TODO this is not used anywhere except one unit test...
   */
  final class FabricExecutionException(message: String, cause: Option[Throwable] = None) extends FabricException(message, cause)

  object FabricExecutionException {
    def apply(message: String): FabricExecutionException = new FabricExecutionException(message, None)
  }

}
