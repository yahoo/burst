/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
 * ← ⇒
 */
//@Ignore
class HydraCubeParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse cube 1" in {
    implicit val source: String =
      s"""|hydra myAnalysis() {
          |  schema unity
          |  frame myFrame {
          |    cube user {
          |      limit = 300
          |      aggregates {
          |        a1:sum[integer]
          |        a2:top[byte](30)
          |      }
          |      dimensions {
          |        d1:verbatim[long]
          |        d2:enum[string]("foo","bar", "goo")
          |        d3:split[double](2.0,3.0, 4.0)
          |      }
          |    }
          |  }
          |}
       """.stripMargin

    parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse cube 2" in {
    implicit val source: String =
      s"""|hydra myAnalysis() {
          | schema unity
          | frame foo {
          |  cube user {
          |    limit = 300
          |    aggregates {
          |      a1:sum[integer]
          |      a2:top[byte](30)
          |    }
          |    dimensions {
          |      d1:verbatim[long]
          |      d2:enum[string]("foo","bar", "goo")
          |      d3:split[double](2.0,3.0, 4.0)
          |    }
          |    cube user.sessions {
          |       aggregates {
          |         a3:unique[long]
          |       }
          |       dimensions {
          |         d4:verbatim[string]
          |       }
          |     }
          |   }
          | }
          |}
       """.stripMargin

    parser printParse (_.parseAnalysis(source, schema))
  }

}
