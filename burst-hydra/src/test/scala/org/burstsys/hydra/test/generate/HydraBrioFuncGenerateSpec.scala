/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraBrioFuncGenerateSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////
  // contains()
  ////////////////////////////////////////////////////////////////////

  it should "generate brio  contains call 1" in {
    implicit val source: String =
      s"""
         |contains(user.interests, 55)
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate brio  contains call 2" in {
    implicit val source: String =
      s"""
         |contains(user.id, 55)
       """.stripMargin
    val caught =
      intercept[FeltException] {
        parser printGeneration (_.parseAnalysis(wrap, schema))
      }
    assert(caught.message.contains("not supported for contains()"))
  }

  it should "generate brio  contains call 3" in {
    implicit val source: String =
      s"""
         |contains(user.interests, 55, 56, (3 + 1))
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  ////////////////////////////////////////////////////////////////////
  // size()
  ////////////////////////////////////////////////////////////////////

  it should "generate brio  size call 1" in {
    implicit val source: String =
      s"""
         |size(user.interests)
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  ////////////////////////////////////////////////////////////////////
  // keys()
  ////////////////////////////////////////////////////////////////////
  it should "generate brio  keys call 1" in {
    implicit val source: String =
      s"""
         |keys(user.parameters)
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  ////////////////////////////////////////////////////////////////////
  // values()
  ////////////////////////////////////////////////////////////////////
  it should "generate brio  values call 1" in {
    implicit val source: String =
      s"""
         |values(user.parameters)
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  ////////////////////////////////////////////////////////////////////
  // value()
  ////////////////////////////////////////////////////////////////////
  /*
    it should "generate brio  value call 1" in {
      implicit val source: String =
        s"""
           |value(user.parameters, "foo")
         """.stripMargin
      parser printGeneration (_.parseFunctionCall(source, schema))
    }
  */
}
