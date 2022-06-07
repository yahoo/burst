/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
  * ← ⇒
  */
//@Ignore
class HydraPathGenerateSpec extends HydraSpecSupport {


  it should "parse path 1" in {
    implicit val source: String =
      s"""
         |user.sessions.parameters["foo"]
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  /*
    it should "parse path 2" in {
      implicit val source: String =
        s"""
           |user
         """.stripMargin
      parser printGeneration (_.parsePathExpression(source, schema))
    }
  */

  /*
    it should "parse path 3" in {
      implicit val source: String =
        s"""
           |user.sessions
         """.stripMargin
      parser printGeneration (_.parsePathExpression(source, schema))
    }
  */

  it should "parse path 4" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, BrioSchema("unity")))
  }


}
