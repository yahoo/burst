/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.press

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press._
import org.burstsys.brio.test.BrioAbstractSpec
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.{brio, tesla}
import org.scalatest.Ignore

@Ignore
class BrioPresserPerformanceSpec extends BrioAbstractSpec {

  "Brio Model" should "be high performance" in {
    val presserSchema = BrioSchema("")
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    for (i <- 0 until 1E12.toInt) {
      val buffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      val sink = BrioPressSink(buffer, dictionary)
      val presser = BrioPresser(sink)
      try {
        presser.press(presserSchema, BrioMockPressSource())
      } finally {
        tesla.buffer.factory.releaseBuffer(buffer)
        brio.dictionary.factory.releaseMutableDictionary(dictionary)
      }
    }
  }


}
