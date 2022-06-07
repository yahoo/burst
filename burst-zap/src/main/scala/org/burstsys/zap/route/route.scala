/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.vitals.reporter.VitalsByteQuantReporter

import scala.language.implicitConversions

/**
 * TODO
 * CLEAN up runtime/accessor mess!!!!
 */
package object route {

  final val partName: String = "route"

  object ZapRouteReporter extends VitalsByteQuantReporter("zap", "route")

}
