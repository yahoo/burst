/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@Ignore
class HydraAnalysisParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse analysis in text form" in {
    implicit val source: String =
      s"""
         |hydra myAnalysis (
         |
         |    // implicit parameters domainKey, viewKey, timeZone
         |    param1:boolean = true,
         |    param2:integer = 1,
         |    param3:map[string, string] = map("hello" -> "goodbye" + param2, "red" → "blue"),
         |    param4:set[double] = set(1.0 / param2, 2.3 * 10),
         |    param5:string = null
         |
         |  ) {
         |    schema quo
         |
         |    val val1:long = 0
         |    var val2:boolean = null // nulls ok!
         |
         |    // first query in analysis
         |    frame myCube {
         |
         |      val val1:integer = 0
         |
         |      cube user {
         |        limit = 300
         |        aggregates {
         |            agg1:sum[integer]
         |            agg2:top[byte](30 + 1)
         |        }
         |        dimensions {
         |            dim1:verbatim[long]
         |        }
         |        cube user.sessions {
         |          aggregates {
         |            agg3:unique[long]
         |          }
         |          dimensions {
         |            dim4:verbatim[string]
         |          }
         |        }
         |      }
         |
         |      // path visits go here...
         |      user ⇒ {
         |        pre ⇒ {
         |          val val2:integer = 0
         |              ???
         |        }
         |        post ⇒ {
         |          val val2:integer = 0
         |              ???
         |        }
         |      }
         |
         |      user.sessions ⇒ {
         |
         |        pre ⇒ {
         |          val val1:double = (0.5 * 12.1) % 3
         |          user.sessions.sessionId match {
         |            case "foo" ⇒ {
         |              val val1:string = "hello there"
         |              ???
         |            }
         |            case 5 ⇒ {
         |              val val2:integer = 0
         |              ???
         |            }
         |            case _ ⇒ {
         |              ???
         |            }
         |          }
         |        }
         |
         |      }
         |
         |    }
         |
         |}
       """.stripMargin

    parser printParse (_.parseAnalysis(source, schema))
  }

}
