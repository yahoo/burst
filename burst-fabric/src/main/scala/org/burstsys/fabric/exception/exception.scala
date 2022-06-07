/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}

package object exception {

  final
  def stitch(remote:Throwable, local:Throwable): Throwable = {
    val currentTrace = Thread.currentThread().getStackTrace
    // skip over internals
    val localStackTrace = currentTrace.takeRight(currentTrace.length - 2)
    val localRemoteBoundary = Array(new StackTraceElement(
      s"---------------- REMOTE STACK ---------------- ",
      "",
      "",
      -1
    ))
    val remoteStackTrace = remote.getStackTrace
    local.setStackTrace(localStackTrace ++ localRemoteBoundary ++ remoteStackTrace)
    local
  }


  /**
    * the base trait for all fabric exceptions
    */
  abstract class FabricException(
                                  message: String,
                                  cause: Option[Throwable],
                                  val hostName: VitalsHostName = vitals.net.getLocalHostName,
                                  val hostAddress: VitalsHostAddress = vitals.net.getLocalHostAddress
                                ) extends RuntimeException(message, cause.orNull) {

    /**
      * create remote/local merged exception stack for throwing on master after receiving serialized
      * stack trace from remote JVM
      *
      * @return
      */
    def stitch: Throwable = {
      val currentTrace = Thread.currentThread().getStackTrace
      // skip over internals
      val localStackTrace = currentTrace.takeRight(currentTrace.length - 2)
      val localRemoteBoundary = Array(new StackTraceElement(
        s"---------------- REMOTE STACK ----- ", "",
        s"HOST '$hostName' $hostAddress",
        -1
      ))
      val remoteStackTrace = getStackTrace
      setStackTrace(localStackTrace ++ localRemoteBoundary ++ remoteStackTrace)
      this
    }
  }

  /**
   * a [[FabricException]] exception originating from an '''uncategorized''' aspect of fabric pipelines
   */
  final class FabricGenericException(message: String, cause: Option[Throwable] = None) extends FabricException(message, cause)

  object FabricGenericException {
    def apply(message: String, cause: Throwable): FabricGenericException = new FabricGenericException(message, Option(cause))

    def apply(cause: Throwable): FabricGenericException =
      new FabricGenericException(if (cause != null) cause.getMessage else "No cause provided", Option(cause))
  }

  // this constructor automatically fills in the stack trace
  class FabricQueryProcessingException(val language: String, msg: String, err: Throwable) extends FabricException(msg, Option(err))

  object FabricQueryProcessingException {
    def apply(language: String, msg: String, err: Throwable): FabricQueryProcessingException =
      new FabricQueryProcessingException(language, msg, err)
  }

}
