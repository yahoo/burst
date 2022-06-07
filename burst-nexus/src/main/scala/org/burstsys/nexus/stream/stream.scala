/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsAgent

import scala.language.postfixOps

package object stream extends VitalsLogger {

  ///////////////////////////////////////////////////////////////////////////
  // UIDs
  ///////////////////////////////////////////////////////////////////////////

  def newRuid: NexusRequestUid = _ruidGenerator.incrementAndGet()

  private
  val _ruidGenerator = new AtomicInteger

  def streamIds(stream: NexusStream) =
    s"stream(guid=${if (stream == null) "null" else stream.guid} suid=${if (stream == null) "null" else stream.guid})"

}
