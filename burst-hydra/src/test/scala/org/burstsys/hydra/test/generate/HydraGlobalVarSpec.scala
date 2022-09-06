/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraGlobalVarSpec extends HydraSpecSupport {


  it should "parse frame variables 1" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:long = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |
         |      pre => {
         |        var lv4:long = gv3
         |        gv3 = lv4       // LINE #1
         |        lv4 = gv3 + gv3  // LINE #2
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  it should "parse frame variables 2" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    val gv3:long = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |
         |      pre => {
         |        val v4:integer = gv3
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  it should "parse frame variables 3" in {
    implicit val source: String =
      s"""
         |hydra $analysisName  () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:long = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |      pre => {
         |        gv3 = gv3 + gv3  // LINE #1
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  it should "parse frame variables 4" in {
    implicit val source: String =
      s"""
         |hydra $analysisName  () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:string = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |
         |      pre => {
         |        gv3 = user.id
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }


  it should "parse frame variables 5" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:string = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |    user => {
         |      pre => {
         |        gv3 = user.id * 3
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  it  should "parse frame variables 6" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:string = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |      pre => {
         |        gv3 = user.id * user.id
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }


  it should "parse frame variables 7" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    var gv3:string = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user => {
         |      pre => {
         |        gv3 = "foo"
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }
}
