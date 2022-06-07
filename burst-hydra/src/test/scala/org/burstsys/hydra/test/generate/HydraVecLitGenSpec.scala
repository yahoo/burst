/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraVecLitGenSpec extends HydraSpecSupport {

  it should "generate complex literal 1" in {
    implicit val source: String =
      s"""|
          |   val lv1:set[byte] = set(3, 5, 10)
          |   val lv2:array[string] = array("foo", "bar")
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "generate complex literal 2" in {
    implicit val source: String =
      s"""|
          |   val lv1:array[double] = array(3, 5, 10.0)
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "generate complex literal 3" in {
    implicit val source: String =
      s"""|
          |   val lv1:set[double] = set()
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  ignore should "generate complex literal 4" in {
    implicit val source: String =
      s"""|
          |   val lv1:array[double] = array()
          |   lv1 = array(3, 5, 10.0)
       """.stripMargin

    val caught = intercept[FeltException] {
      parser printGeneration (_.parseAnalysis(wrap, schema))
    }
    assert(caught.message.contains("'lv1 = array(3, 5, 10.0)' assignment not possible to immutable lhs"))
  }


}
