/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._

package object test extends VitalsLogger {

  def messageWithLine(): String = {
    burstStdMsg("Message line")
  }

  def messageWithLocation(): String = {
    burstLocMsg("Message with location")
  }

  def exceptionLocation(): Nothing = throw VitalsException("Exception location")

  case class PackageCaseClass() {
    def message(): String = burstLocMsg("From package case class")
  }
}
