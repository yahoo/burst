/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraMapLitGenSpec extends HydraSpecSupport {

  it should "generate complex literal 1" in {
    implicit val source: String =
      s"""|
          |   val lv1:map[byte, byte] = map(3 -> 0, 5 -> 0, 10 -> 0)
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "generate complex literal 2" in {
    implicit val source: String =
      s"""|
          |   val lv1:map[integer, short] = map(3 -> 0, 5 -> 0, 10 -> 0)
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "generate complex literal 3" in {
    implicit val source: String =
      s"""|
          |   val lv1:map[long, long] = map()
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  ignore should "generate complex literal 4" in {
    implicit val source: String =
      s"""|
          |   var lv1:map[double, string] = map()
          |   lv1 = map(3 -> "foo1", 5.0 -> "foo2", 10 -> "foo3")
       """.stripMargin
    val caught = intercept[FeltException] {
      val e = parser printGeneration (_.parseAnalysis(wrap, schema))
      e
    }
    assert(caught.message.contains("maps and vectors can't be mutable (must be val)"))
  }

  it should "generate complex literal 5" in {
    implicit val source: String =
      s"""|
          |   val lv1:map[double, string] = map(3 -> "foo1", 5.0 -> "foo2", 10 -> "foo3")
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


}
