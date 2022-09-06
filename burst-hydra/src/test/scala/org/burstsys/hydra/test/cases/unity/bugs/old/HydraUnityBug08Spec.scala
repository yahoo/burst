/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraUnityBug08Spec extends HydraAlloyTestRunner {

  //  doSerializeTraversal = false
  //  useStaticSweep = null

  it should "successfully define an extended path sub cube" in {
    val source =
      s"""
         |hydra eqlGenerated() {
         |	schema 'unity'
         |	frame test {
         |		var T3:boolean = false
         |		var T2:boolean = false
         |		var T1:boolean = false
         |		var T4:boolean = false
         |		route {
         |			maxPartialPaths = 1
         |			maxSteps = 100
         |			graph {
         |				 7 {
         |					to(8, 0, 0)
         |				}
         |				 8 {
         |					to(9, 0, 0)
         |				}
         |				 9 {
         |					to(10, 0, 0)
         |				}
         |				 10 {
         |					to(11, 0, 0)
         |				}
         |				exit 11 {
         |				}
         |			}
         |		}
         |		user.sessions  => {
         |			var route_test_control:boolean = false
         |			pre => 			{
         |				routeScopeStart(test)
         |				T1 = true
         |				if( T1 )
         |				{
         |					route_test_control = route_test_control || (					routeFsmStepAssert(test, 7, 1, user.sessions.startTime))
         |				}
         |				if( ! route_test_control )
         |				{
         |					if( T1 )
         |					{
         |						if( 						routeFsmInStep(test, 10) )
         |						{
         |							routeFsmStepAssert(test, 11, 0, 0)
         |						}
         |						else
         |						{
         |							routeFsmEndPath(test)
         |						}
         |					}
         |					if( ! route_test_control && T1 )
         |					{
         |						route_test_control = route_test_control || (						routeFsmStepAssert(test, 7, 1, user.sessions.startTime))
         |					}
         |				}
         |				routeScopeCommit(test)
         |			}
         |		}
         |		user.sessions.events  => {
         |			var route_test_control:boolean = false
         |			pre => 			{
         |				routeScopeStart(test)
         |				T4 = (user.sessions.events.id  in (5, 6))
         |				if( T4 )
         |				{
         |					route_test_control = route_test_control || (					routeFsmStepAssert(test, 10, 4, user.sessions.events.startTime))
         |				}
         |				T3 = (user.sessions.events.id  in (3, 4))
         |				if( T3 )
         |				{
         |					route_test_control = route_test_control || (					routeFsmStepAssert(test, 9, 3, user.sessions.events.startTime))
         |				}
         |				T2 = (user.sessions.events.id  in (1, 2))
         |				if( T2 )
         |				{
         |					route_test_control = route_test_control || (					routeFsmStepAssert(test, 8, 2, user.sessions.events.startTime))
         |				}
         |				if( ! route_test_control )
         |				{
         |					if( T3 || T4 || T2 )
         |					{
         |						if( 						routeFsmInStep(test, 10) )
         |						{
         |							routeFsmStepAssert(test, 11, 0, 0)
         |						}
         |						else
         |						{
         |							routeFsmEndPath(test)
         |						}
         |					}
         |				}
         |				routeScopeCommit(test)
         |			}
         |		}
         |	}
         |	frame query_test {
         |		cube user {
         |			limit = 100
         |			aggregates {
         |				'num':sum[long]
         |			}
         |			dimensions {
         |				'ids':verbatim[long]
         |			}
         |		}
         |		test.paths.steps user  => {
         |			pre => 			{
         |				eqlGenerated.query_test.ids = 				routeVisitStepTag(test)
         |			}
         |			post => 			{
         |				eqlGenerated.query_test.num = 1
         |			}
         |		}
         |	}
         |}
         |""".stripMargin

    test(
      source, over_200_200, {
        (name, result) =>
      }
    )
  }

}
