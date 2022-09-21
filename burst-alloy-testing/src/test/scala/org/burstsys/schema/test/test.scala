/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.schema

import org.burstsys.brio
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {

  abstract class AbstractBrioSchemaSpec extends AnyFlatSpec with Matchers {

    VitalsLog.configureLogging("schema", consoleOnly = true)

  }

}
