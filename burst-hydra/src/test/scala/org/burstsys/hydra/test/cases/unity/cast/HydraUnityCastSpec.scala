/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.cast

import org.burstsys.alloy.views.unity.UnityEventParamViews.fixed_event_parameters
import org.burstsys.alloy.views.unity.UnityEventParamViews.float_event_parameters
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

class HydraUnityCastSpec extends HydraAlloyTestRunner {

  it should "cast double values to strings" in {
    val source =
      s"""
         |hydra MyAnalysis() {
         |  schema unity
         |  frame MyFrame  {
         |    cube user {
         |      limit = 999999
         |      dimensions {
         |        'cast':verbatim[string]
         |      }
         |    }
         |    user.sessions.events.parameters => {
         |      situ => {
         |        var v1:double = cast( value(user.sessions.events.parameters) as double )
         |        MyFrame.'cast' =  cast(v1 as string)
         |        insert( MyFrame)
         |      }
         |    }
         |  }
         |}""".stripMargin

    test(
      hydra = source,
      ds = float_event_parameters,
      validate = {
        (name, result) =>
          result.resultName match {
            case "MyFrame" =>
              (result.rowSet.map {
                row =>
                  row[String]("cast")
              }).sorted should equal(Array("1.1", "2.2", "3.3", "4.4", "5.5", "6.6", "7.7"))
            case _ =>
          }
      },
      staticSweep = None // Some(new BC2D234DC9DCA4438AD99EDA831758977)
    )
  }

  it should "cast double values to double via string" in {
    val source =
      s"""
         |hydra MyAnalysis() {
         |  schema unity
         |  frame MyFrame  {
         |    cube user {
         |      limit = 999999
         |      dimensions {
         |        'final':verbatim[double]
         |      }
         |    }
         |    user.sessions.events.parameters => {
         |      situ => {
         |        val v1:double = cast(value(user.sessions.events.parameters) as double )
         |        val v2:string =  cast(v1 as string)
         |        MyFrame.'final' =  cast(v2 as double)
         |        insert( MyFrame)
         |      }
         |    }
         |  }
         |}""".stripMargin

    test(
      hydra = source,
      ds = float_event_parameters,
      validate = {
        (name, result) =>
          result.resultName match {
            case "MyFrame" =>
              (result.rowSet.map {
                row =>
                  row[Double]("final")
              }).sorted should equal(Array(1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7))
            case _ =>
          }
      },
      staticSweep = None // Some(new BC2D234DC9DCA4438AD99EDA831758977)
    )
  }

  it should "direct string to double agg update cast" in {
    val source =
      s"""
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      cube user {
          |         limit = 300000
          |         aggregates {
          |            pVal:sum[double]
          |         }
          |      }
          |      user.sessions.events => {
          |         post => {
          |               eqlGenerated.query_1.pVal = cast(user.sessions.events.parameters["K1"] as double)
          |         }
          |      }
          |   }
          |}
          |""".stripMargin


    test(source, float_event_parameters, {
      (name, result) =>
        name match {
          case "query_1" =>
            val found = result.rowSet.map {
              row =>
                (
                  row[Double]("pVal")
                  )
            }.sorted
            val expected = Array(2.2)
            found should equal(expected)
          case _ =>
        }
    },
      staticSweep = None // Some(new B6EA6710919C840979929D66C6E6B3A59)
    )
  }

  it should "cast parameter values to doubles" in {
    val source =
      s"""
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      cube user {
          |         limit = 1000
          |         aggregates {
          |            'count':sum[long]
          |         }
          |         dimensions {
          |            'value':verbatim[double]
          |         }
          |      }
          |      user.sessions.events => {
          |         pre => {
          |            T1 = user.sessions.events.parameters["K1"] == "1.1"
          |            if (T1) {
          |               query_1.'value' = cast(user.sessions.events.parameters["K1"] as double)
          |            }
          |         }
          |         post => {
          |            if (T1) {
          |               T1_summary = true
          |               eqlGenerated.query_1.count = 1
          |            }
          |         }
          |      }
          |   }
          |}""".stripMargin

    test(
      hydra = source,
      ds = float_event_parameters,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_1" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Double]("value"),
                    row[Long]("count")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected = Array((1.1, 2))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B0594910A48404B29A6B204AFB8A0AE39)
    )
  }

  it should "cast string parameter values to longs" in {
    val source =
      s"""
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      cube user {
          |         limit = 1000
          |         aggregates {
          |            'count':sum[long]
          |         }
          |         dimensions {
          |            'value':verbatim[long]
          |         }
          |      }
          |      user.sessions.events => {
          |         pre => {
          |            T1 = user.sessions.events.parameters["K1"] == "1"
          |            if (T1) {
          |               query_1.'value' = cast(user.sessions.events.parameters["K1"] as long)
          |            }
          |         }
          |         post => {
          |            if (T1) {
          |               T1_summary = true
          |               eqlGenerated.query_1.count = 1
          |            }
          |         }
          |      }
          |   }
          |}""".stripMargin

    test(
      hydra = source,
      ds = fixed_event_parameters,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_1" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("value"),
                    row[Long]("count")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected = Array((1, 2))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B0594910A48404B29A6B204AFB8A0AE39)
    )
  }

}


