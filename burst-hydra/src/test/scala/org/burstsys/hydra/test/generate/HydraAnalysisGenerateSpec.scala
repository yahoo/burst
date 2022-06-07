/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@Ignore
class HydraAnalysisGenerateSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse analysis in text form" in {
    implicit val source: String =
      s"""
         hydra myAnalysis (
             // implicit parameters domainKey, viewKey, timeZone
             p1:boolean = true,
             p2:long = 1,
             p3:map[string, string] = map("hello" -> "goodbye", "red" → "blue"),
             p4:array[double] = array(1.0, 2.3),
             p5:string = null

           ) {
             schema org.burstsys.'schema'.quo

             val v1:long = 0
             var v2:boolean = null // nulls ok!

             // first frame in analysis
             frame myFrame {

               val v3:long = 0

               cube user {
                 limit = 300
                 aggregates {
                     a1:sum[integer]
                     a2:top[byte](30 + 1)
                     a3:unique[long]
                     a4:max[long]
                     a5:min[double]
                 }
                 dimensions {
                     d1:verbatim[long]
                     d2:enum[string]("red", "green", "other")
                     d3:split[double](-1.0, 0.0, 1.0)
                     d4:dayGrain[long]
                     d5:yearOfEraOrdinal[long]
                 }
                 cube user.sessions {
                   aggregates {
                     a3:unique[long]
                   }
                   dimensions {
                     d4:verbatim[string]
                   }
                 }
               }
             }

             // second frame in analysis
             frame myRoute {
               route {
                 maxPartialPaths = 10
                 maxSteps = 30
                 graph {
                   enter 1 {
                     to(2)
                     to(3, 0, 0)
                   }
                   exit 2 {
                   }
                   exit 3 {
                   }
                 }
               }

               // path visits go here...
               user ⇒ {
                 pre ⇒ {
                   val v4:integer = 0 + 0 * 1

                   if(true) {
                    val e:boolean = true

                   }
                       ???
                 }
                 post ⇒ {
                       ???
                 }
               }

               user.sessions ⇒ {

                 pre ⇒ {
                   val bar:double = (0.5 * 12.1) % 3
                   user.sessions.sessionId match {
                     case "foo" ⇒ {
                       val foo:string = "hello there"
                       ???
                     }
                     case 5 ⇒ {
                       ???
                     }
                     case _ ⇒ {
                       ???
                     }
                   }
                 }

               }

             }

         }
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  it should "generate analysis in sweep form" in {
    implicit val source: String =
      s"""
         hydra myAnalysis (
             // implicit parameters domainKey, viewKey, timeZone
             p1:boolean = true,
             p2:long = 1,
             p3:map[string, string] = map("hello" -> "goodbye", "red" → "blue"),
             p4:array[double] = array(1.0, 2.3),
             p5:string = null

           ) {
             schema org.burstsys.'schema'.quo

             val v1:long = 0
             var v2:boolean = null // nulls ok!

             // first frame in analysis
             frame myCubeFrame {

               val v3:long = 0

               cube user {
                 limit = 300
                 aggregates {
                     a1:sum[integer]
                     a2:top[byte](30 + 1)
                     a3:unique[long]
                     a4:max[long]
                     a5:min[double]
                 }
                 dimensions {
                     d1:verbatim[long]
                     d2:enum[string]("red", "green", "other")
                     d3:split[double](-1.0, 0.0, 1.0)
                     d4:dayGrain[long]
                     d5:yearOfEraOrdinal[long]
                 }
                 cube user.sessions {
                   aggregates {
                     a3:unique[long]
                   }
                   dimensions {
                     d4:verbatim[string]
                   }
                 }
               }
             }
             // first frame in analysis
             frame myRouteFrame {
               route {
                 maxPartialPaths = 10
                 maxSteps = 30
                 graph {
                   enter 1 {
                     to(2)
                     to(3, 0, 0)
                   }
                   exit 2 {
                   }
                   exit 3 {
                   }
                 }
               }

               // path visits go here...
               user ⇒ {
                 pre ⇒ {
                   val v4:integer = 0
                       ???
                 }
                 post ⇒ {
                       ???
                 }
               }

               user.sessions ⇒ {

                 pre ⇒ {
                   val bar:double = (0.5 * 12.1) % 3
                   user.sessions.sessionId match {
                     case "foo" ⇒ {
                       val foo:string = "hello there"
                       ???
                     }
                     case 5 ⇒ {
                       ???
                     }
                     case _ ⇒ {
                       ???
                     }
                   }
                 }

               }

             }

         }
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))

  }


}
