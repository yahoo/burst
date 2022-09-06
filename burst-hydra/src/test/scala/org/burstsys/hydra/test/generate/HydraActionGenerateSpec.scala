/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.tree.code.FeltCodeCursor
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraActionGenerateSpec extends HydraSpecSupport {


  it should "generate bare bones action" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |
         |  // global variables!!
         |  val g1:double = 1.0 + 2.0
         |
         |  frame $frameName {
         |
         |    // frame level variables!!
         |    val q1:boolean = true
         |
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[long]
         |      }
         |      dimensions {
         |        d1:verbatim[long]
         |      }
         |    } // end cube user
         |
         |    user.sessions => {
         |      // visit level variables!!
         |      val u1:boolean = true
         |      pre => {
         |        // action level variables!!
         |        val foo:string = "hello there"
         |        if(user.sessions.id != 34) {
         |          $analysisName.$frameName.a1 = dayGrain(user.sessions.startTime)
         |          $analysisName.$frameName.d1 = dayGrain(user.sessions.startTime) - now()
         |          // terminate
         |          // exit
         |        }
         |      } // end pre action
         |    } // end user visit
         |  } // end frame mock
         |}
       """.stripMargin

    val foo = parser printGeneration (_.parseAnalysis(analysisSource = source, defaultSchema = schema))
    foo
  }


  it should "parse action 1" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[long]
         |      }
         |      dimensions {
         |        d1:verbatim[long]
         |      }
         |    } // end cube user
         |
         |    user.sessions => {
         |      pre => {
         |        val foo:string = "hello there"
         |        if( user.sessions.id == null ) {
         |          $analysisName.$frameName.a1 = dayGrain(user.sessions.id)
         |          $analysisName.$frameName.d1 = dayGrain(user.sessions.id) - now()
         |          // terminate
         |          // exit
         |        }
         |      } // end pre action
         |      post => {
         |        // all ginsu functions
         |        dayGrain(user.sessions.startTime) - dayGrain(user.sessions.startTime)
         |        hourGrain(user.sessions.startTime)- halfGrain(user.sessions.startTime)
         |        minuteGrain(user.sessions.startTime)- hourGrain(user.sessions.startTime)
         |        secondGrain(user.sessions.startTime)- monthGrain(user.sessions.startTime)
         |        weekGrain(user.sessions.startTime)- quarterGrain(user.sessions.startTime)
         |        weekGrain(user.sessions.startTime)- yearGrain(user.sessions.startTime)
         |        dayOfMonthOrdinal(user.sessions.startTime)- dayOfWeekOrdinal(user.sessions.startTime)
         |        hourOfDayOrdinal(user.sessions.startTime)- monthOfYearOrdinal(user.sessions.startTime)
         |        weekOfYearOrdinal(user.sessions.startTime)- yearOfEraOrdinal(user.sessions.startTime)
         |      }
         |    } // end user visit
         |    user.sessions => {
         |      before => {
         |        val i:long = (4*5)
         |      }
         |      after => {
         |        cast(user.sessions.startTime as string)
         |      }
         |    }
         |    user.sessions.parameters => {
         |      situ => {
         |        val i:long = (4*5)
         |      }
         |    }
         |  } // end frame mock
         |}
       """.stripMargin

    val foo = parser printGeneration (_.parseAnalysis(analysisSource = source, defaultSchema = schema))
    foo
  }


}
