/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes

import org.burstsys.alloy.views.AlloySmallDatasets.{smallDataset_2_users_5_sessions, smallDataset_one_user_one_session, smallDataset_one_user_two_sessions}
import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

import scala.language.postfixOps

//@Ignore
final
class HydraRouteSpec extends HydraAlloyTestRunner {

  it should "record all step functions" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'pathOrdinal':verbatim[long]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'stepTag':verbatim[long]
          |      'stepTime':verbatim[long]
          |    }
          |  }
          |  $RouteFrame.paths.steps user.ext1 (4) => {
          |    situ => {
          |      $CubeFrame.'pathOrdinal' = routeVisitPathOrdinal( $RouteFrame )
          |      $CubeFrame.'stepOrdinal' = routeVisitStepOrdinal( $RouteFrame )
          |      $CubeFrame.'stepKey' = routeVisitStepKey( $RouteFrame )
          |      $CubeFrame.'stepTag' = routeVisitStepTag( $RouteFrame )
          |      $CubeFrame.'stepTime' = routeVisitStepTime( $RouteFrame )
          |      insert( $CubeFrame )
          |    }
          |  }
          | }
          | frame $RouteFrame {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {  enter 1 { to(2) } exit 2 { } }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( $RouteFrame )
          |        routeFsmStepAssert( $RouteFrame, 1, 101, 1111 )
          |        routeFsmStepAssert( $RouteFrame, 2, 101, 1111 )
          |        routeScopeCommit( $RouteFrame )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val found = result.rowSet.map {
                row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong, row.cells(3) asLong, row.cells(4) asLong)
              } sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val columns = result.columnNames // "pathOrdinal", "stepOrdinal", "stepKey", "stepTag", "stepTime"
              val expected =
                Array((1,0,1,101,1111), (1,1,2,101,1111), (2,0,1,101,1111), (2,1,2,101,1111), (3,0,1,101,1111), (3,1,2,101,1111), (4,0,1,101,1111), (4,1,2,101,1111), (5,0,1,101,1111), (5,1,2,101,1111))

              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B25C84BD3115F4BACA862D4F6162FBB17)
    )
  }

  // TODO: the isLastStep dimension seems to never turns true anymore, but I'd point out that the isLastPath and isFirstPath
  // are were never true before either which seems wrong.
  ignore should "use first/last step functions" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user.sessions <- ext1
          | }
          | frame $CubeFrame {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'stepKey':verbatim[long]
          |      'isFirstStep':verbatim[boolean]
          |      'isLastStep':verbatim[boolean]
          |      'isFirstPath':verbatim[boolean]
          |      'isLastPath':verbatim[boolean]
          |      'sessionId':verbatim[long]
          |    }
          |  }
          |  $RouteFrame.paths.steps user.sessions.ext1 (4) => {
          |    situ => {
          |      $CubeFrame.'stepKey' = routeVisitStepKey( $RouteFrame )
          |      $CubeFrame.'isFirstStep' = routeVisitStepIsFirst( $RouteFrame )
          |      $CubeFrame.'isLastStep'  = routeVisitStepIsLast( $RouteFrame )
          |      $CubeFrame.'isFirstPath' = routeVisitPathIsFirst( $RouteFrame )
          |      $CubeFrame.'isLastPath'  = routeVisitPathIsLast( $RouteFrame )
          |      $CubeFrame.'sessionId'   = user.sessions.id
          |      insert( $CubeFrame )
          |    }
          |  }
          | }
          | frame $RouteFrame {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {  enter 1 { to(2) }  2 { to(3) }  3 { to(4)  } exit 4 { } }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( $RouteFrame )
          |        routeFsmStepAssert( $RouteFrame, 1, 101, 1001 )
          |        routeFsmStepAssert( $RouteFrame, 2, 102, 1002 )
          |        routeFsmStepAssert( $RouteFrame, 3, 103, 1003 )
          |        routeFsmStepAssert( $RouteFrame, 4, 104, 1004 )
          |        routeScopeCommit( $RouteFrame )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val columns = result.columnNames //["stepKey", "isFirstStep", "isLastStep", "isFirstPath", "isLastPath", sessionId]
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("sessionId"),
                    row[Long]("stepKey"),
                    row[Boolean]("isFirstStep"),
                    row[Boolean]("isLastStep"),
                    row[Boolean]("isFirstPath"),
                    row[Boolean]("isLastPath")
                  )
              } sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (0, 1, true, false, true, false), (0, 2, false, false, false, false), (0, 3, false, false, false, false), (0, 4, false, true, false, false),
                  (1, 1, true, false, true, false), (1, 2, false, false, false, false), (1, 3, false, false, false, false), (1, 4, false, true, false, false),
                  (2, 1, true, false, true, false), (2, 2, false, false, false, false), (2, 3, false, false, false, false), (2, 4, false, true, false, false),
                  (3, 1, true, false, true, false), (3, 2, false, false, false, false), (3, 3, false, false, false, false), (3, 4, false, true, false, false),
                  (4, 1, true, false, true, false), (4, 2, false, false, false, false), (4, 3, false, false, false, false), (4, 4, false, true, false, false),
                  (5, 1, true, false, true, false), (5, 2, false, false, false, false), (5, 3, false, false, false, false), (5, 4, false, true, false, false),
                  (6, 1, true, false, true, false), (6, 2, false, false, false, false), (6, 3, false, false, false, false), (6, 4, false, true, false, false),
                  (7, 1, true, false, true, false), (7, 2, false, false, false, false), (7, 3, false, false, false, false), (7, 4, false, true, false, false),
                  (8, 1, true, false, true, false), (8, 2, false, false, false, false), (8, 3, false, false, false, false), (8, 4, false, true, false, false),
                  (9, 1, true, false, true, false), (9, 2, false, false, false, false), (9, 3, false, false, false, false), (9, 4, false, true, false, false)
                )

              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // new B25C84BD3115F4BACA862D4F6162FBB17,
    )
  }

  it should "successfully do a simple route visit" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'pathOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'stepTag':verbatim[long]
          |      'stepTime':verbatim[long]
          |    }
          |  }
          |  $RouteFrame.paths.steps user.ext1 (4) => {
          |    situ => {
          |      $CubeFrame.'pathOrdinal' = routeVisitPathOrdinal( $RouteFrame )
          |      $CubeFrame.'stepKey' = routeVisitStepKey( $RouteFrame )
          |      $CubeFrame.'stepTag' = routeVisitStepTag( $RouteFrame )
          |      $CubeFrame.'stepTime' = routeVisitStepTime( $RouteFrame )
          |      insert( $CubeFrame )
          |    }
          |  }
          | }
          | frame $RouteFrame {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {  enter 1 { to(2) } exit 2 { } }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( $RouteFrame )
          |        routeFsmStepAssert( $RouteFrame, 1, 101, 1111 )
          |        routeFsmStepAssert( $RouteFrame, 2, 101, 1111 )
          |        routeScopeCommit( $RouteFrame )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(source, over_200_200, {
      (name, result) =>
        name match {
          case CubeFrame =>
            val found = result.rowSet.map {
              row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong, row.cells(3) asLong)
            } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
            val expected = Array(
              (1, 1, 101, 1111), (2, 1, 101, 1111), (3, 1, 101, 1111), (4, 1, 101, 1111), (5, 1, 101, 1111), (6, 1, 101, 1111), (7, 1, 101, 1111),
              (8, 1, 101, 1111), (9, 1, 101, 1111), (10, 1, 101, 1111), (11, 1, 101, 1111), (12, 1, 101, 1111), (13, 1, 101, 1111),
              (14, 1, 101, 1111), (15, 1, 101, 1111), (16, 1, 101, 1111), (17, 1, 101, 1111), (18, 1, 101, 1111), (19, 1, 101, 1111),
              (20, 1, 101, 1111), (21, 1, 101, 1111), (22, 1, 101, 1111), (23, 1, 101, 1111), (24, 1, 101, 1111), (25, 1, 101, 1111),
              (1, 2, 101, 1111), (2, 2, 101, 1111), (3, 2, 101, 1111), (4, 2, 101, 1111), (5, 2, 101, 1111), (6, 2, 101, 1111), (7, 2, 101, 1111),
              (8, 2, 101, 1111), (9, 2, 101, 1111), (10, 2, 101, 1111), (11, 2, 101, 1111), (12, 2, 101, 1111), (13, 2, 101, 1111),
              (14, 2, 101, 1111), (15, 2, 101, 1111), (16, 2, 101, 1111), (17, 2, 101, 1111), (18, 2, 101, 1111), (19, 2, 101, 1111),
              (20, 2, 101, 1111), (21, 2, 101, 1111), (22, 2, 101, 1111), (23, 2, 101, 1111), (24, 2, 101, 1111), (25, 2, 101, 1111)
            )
            found should equal(expected)
          case _ =>
        }
    }
    )
  }

  it should "successfully record a path with a complete trait and find all steps in that path are in fact complete" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'isComplete':verbatim[boolean]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |    }
          |  }
          |  $RouteFrame.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {  enter 1 { to(2) } 2 { to(3) } complete, exit 3 { } }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("pathOrdinal"), row[Long]("stepKey"), row[Boolean]("isComplete"))
              } sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (1, 1, true), (1, 2, true), (1, 3, true),
                  (2, 1, true), (2, 2, true), (2, 3, true),
                  (3, 1, true), (3, 2, true), (3, 3, true),
                  (4, 1, true), (4, 2, true), (4, 3, true),
                  (5, 1, true), (5, 2, true), (5, 3, true)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B53F39494BD91407FABA1F0D7057CD468)
    )
  }

  it should "successfully record a path with a complete trait and an exit step (single session)" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |    }
          |  }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) to(4) }
          |       exit 3 { }
          |       complete 4 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 4, 101, 1111 )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Long]("stepOrdinal"),
                    row[Long]("stepKey"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths")
                  )
              } sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,0,1,false,2), (1,1,2,false,2), (1,2,3,false,2), (2,0,1,true,2), (2,1,2,true,2), (2,2,4,true,2))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B7CED3479EA8E4AA3B7E66B9947D006E1)
    )
  }

  it should "successfully record a path with a complete trait and an exit step (two sessions)" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 9999
          |     aggregates {
          |       stepCount:sum[long]
          |   }
          |   dimensions {
          |      'userId':verbatim[string]
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |   }
          | }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'userId' = user.id
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      myCube.'stepCount' = 1
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) to(4) }
          |       exit 3 { }
          |       complete 4 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute ) // noop
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 4, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_two_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Long]("stepOrdinal"),
                    row[Long]("stepKey"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths"),
                    row[Long]("stepCount")
                  )
              } sortBy (_._6) sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,0,1,false,4,1), (1,1,2,false,4,1), (1,2,3,false,4,1), (2,0,1,true,4,1), (2,1,2,true,4,1), (2,2,4,true,4,1), (3,0,1,false,4,1), (3,1,2,false,4,1), (3,2,3,false,4,1), (4,0,1,true,4,1), (4,1,2,true,4,1), (4,2,4,true,4,1))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
    )
  }

  it should "record a path with a tacit intermediate step, and a complete trait and an exit step (two sessions)" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |   cube user {
          |     limit = 9999
          |     aggregates {
          |       stepCount:sum[long]
          |     }
          |     dimensions {
          |      'userId':verbatim[string]
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |     }
          | }
          | myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'userId' = user.id
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      myCube.'stepCount' = 1
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {
          |       enter 1 { to(2) }
          |       tacit 2 { to(3) to(4) }
          |       exit 3 { }
          |       complete 4 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute ) // noop
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 4, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_two_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Long]("stepOrdinal"),
                    row[Long]("stepKey"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths"),
                    row[Long]("stepCount")
                  )
              } sortBy (_._6) sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,0,1,false,4,1), (1,1,3,false,4,1), (2,0,1,true,4,1), (2,1,4,true,4,1), (3,0,1,false,4,1), (3,1,3,false,4,1), (4,0,1,true,4,1), (4,1,4,true,4,1))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
    )
  }

  it should "record a path with a tacit intermediate step, and a complete trait and an exit step (two sessions) with complete path function" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |   limit = 9999
          |   aggregates {
          |       stepCount:sum[long]
          |   }
          |   dimensions {
          |      'userId':verbatim[string]
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |   }
          | }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'userId' = user.id
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      myCube.'stepCount' = 1
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {
          |       enter 1 { to(2) }
          |       tacit 2 { to(3) to(4) }
          |       exit 3 { }
          |       complete 4 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute ) // NOOP
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 4, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_two_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Long]("stepOrdinal"),
                    row[Long]("stepKey"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths"),
                    row[Long]("stepCount")
                  )
              } sortBy (_._6) sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,0,1,false,4,1), (1,1,3,false,4,1), (2,0,1,true,4,1), (2,1,4,true,4,1), (3,0,1,false,4,1), (3,1,3,false,4,1), (4,0,1,true,4,1), (4,1,4,true,4,1))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
    )
  }

  it should "record single step graph with enter/complete traits" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |   limit = 9999
          |   aggregates {
          |       stepCount:sum[long]
          |   }
          |   dimensions {
          |      'userId':verbatim[string]
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |   }
          | }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'userId' = user.id
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      myCube.'stepCount' = 1
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {
          |       enter, complete 1 {   }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_two_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Long]("stepOrdinal"),
                    row[Long]("stepKey"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths"),
                    row[Long]("stepCount")
                  )
              } sortBy (_._6) sortBy (_._5) sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,0,1,true,2,1), (2,0,1,true,2,1))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
    )
  }

  it should "successfully record a path with a maxCompletePaths" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'isComplete':verbatim[boolean]
          |      'stepOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'pathOrdinal':verbatim[long]
          |      'completePaths':verbatim[long]
          |    }
          |  }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'pathOrdinal' = routeVisitPathOrdinal( myRoute )
          |      myCube.'stepOrdinal' = routeVisitStepOrdinal( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      myCube.'isComplete' = routeVisitPathIsComplete( myRoute )
          |      myCube.'completePaths' = routeCompletePaths( myRoute )
          |      insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    maxPartialPaths = 1000
          |    maxCompletePaths = 1
          |    maxSteps = 1000
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) to(4) }
          |       exit 3 { }
          |       complete 4 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 4, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |} """.stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("pathOrdinal"),
                    row[Boolean]("isComplete"),
                    row[Long]("completePaths")
                  )
              } sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (1, true, 1), (1, true, 1), (1, true, 1)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B77698C6CC9B34879BDC763E131281609)
    )
  }

  it should "successfully test for last step in path using exit" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 10
          |    dimensions {
          |      'lastStepInPath':verbatim[boolean]
          |      'stepKey':verbatim[long]
          |    }
          |  }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'lastStepInPath' = routeVisitStepIsLastInPath( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) }
          |       exit 3 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |} """.stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("stepKey"),
                    row[Boolean]("lastStepInPath")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,false), (2,false), (3,true))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B77698C6CC9B34879BDC763E131281609)
    )
  }

  it should "successfully test for last step in path using complete" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |  cube user {
          |    limit = 10
          |    dimensions {
          |      'lastStepInPath':verbatim[boolean]
          |      'stepKey':verbatim[long]
          |    }
          |  }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |      myCube.'lastStepInPath' = routeVisitStepIsLastInPath( myRoute )
          |      myCube.'stepKey' = routeVisitStepKey( myRoute )
          |      insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) }
          |       complete 3 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |} """.stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("stepKey"),
                    row[Boolean]("lastStepInPath")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1, false), (2, false), (3, true))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B77698C6CC9B34879BDC763E131281609)
    )
  }

  it should "do the route visit before the user post" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | var foo: boolean = false
          | frame myCube {
          |  cube user {
          |    limit = 10
          |    dimensions {
          |      order:verbatim[long]
          |      flag:verbatim[boolean]
          |    }
          |  }
          |  user => {
          |     post => {
          |       myCube.order = 1
          |       myCube.flag = foo
          |       insert( myCube )
          |     }
          |  }
          |  myRoute.paths.steps user.ext1 (4) => {
          |    situ => {
          |       myCube.order = 0
          |       myCube.flag = false
          |       foo = true
          |       insert( myCube )
          |    }
          |  }
          | }
          | frame myRoute {
          |  route {
          |    graph {
          |       enter 1 { to(2) }
          |       2 { to(3) }
          |       complete 3 { }
          |    }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( myRoute )
          |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 2, 101, 1111 )
          |        routeFsmStepAssert( myRoute, 3, 101, 1111 )
          |        routeFsmEndPath( myRoute )
          |        routeScopeCommit( myRoute )
          |    }
          |  }
          | }
          |} """.stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("order"),
                    row[Boolean]("flag")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (0, false),
                  (1, true)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B9C06F9E827614E4FA82DA8895CAC7FF6)
    )
  }

  it should "timeout a path greater than maxPathTime" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          | schema unity {
          |   user <- ext1
          | }
          | frame myCube {
          |   cube user {
          |     limit = 10
          |     dimensions {
          |       count:verbatim[long]
          |       flag:verbatim[boolean]
          |     }
          |  }
          |  myRoute.paths.steps user.ext1  => {
          |    situ => {
          |       myCube.count = 0
          |       myCube.flag = true
          |       insert( myCube )
          |    }
          |  }
          | } // end of myCube
          |
          | frame myRoute {
          |   route {
          |     maxPathTime=100
          |     graph {
          |       enter 1 { to(2) }
          |       2 { to(3) }
          |       complete 3 { }
          |     }
          |   }
          |   user.sessions  => {
          |     pre => {
          |       routeScopeStart( myRoute )
          |       routeFsmStepAssert( myRoute, 1, 101, 0 )
          |       routeFsmStepAssert( myRoute, 2, 101, 1000 )
          |       routeFsmStepAssert( myRoute, 3, 101, 5000 )
          |       routeScopeCommit( myRoute )
          |     }
          |   }
          | } // end of myRoute
          |
          |} // end of myAnalysis""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("count"),
                    row[Boolean]("flag")
                  )
              } sortBy (_._1) sortBy (_._2)
              val expected =
                Array(
                  (0, true)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B9C06F9E827614E4FA82DA8895CAC7FF6)
    )
  }

  it should "successfully do a route with large constraints" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |  cube user {
          |    limit = 9999
          |    dimensions {
          |      'pathOrdinal':verbatim[long]
          |      'stepKey':verbatim[long]
          |      'stepTag':verbatim[long]
          |      'stepTime':verbatim[long]
          |    }
          |  }
          |  $RouteFrame.paths.steps user.ext1 (4) => {
          |    situ => {
          |      $CubeFrame.'pathOrdinal' = routeVisitPathOrdinal( $RouteFrame )
          |      $CubeFrame.'stepKey' = routeVisitStepKey( $RouteFrame )
          |      $CubeFrame.'stepTag' = routeVisitStepTag( $RouteFrame )
          |      $CubeFrame.'stepTime' = routeVisitStepTime( $RouteFrame )
          |      insert( $CubeFrame )
          |    }
          |  }
          | }
          | frame $RouteFrame {
          |  route {
          |    maxPartialPaths = 1000
          |    maxSteps = 1000
          |    graph {  enter 1 { to(2, 2592000000L, 0) } exit 2 { } }
          |  }
          |  user.sessions (3) => {
          |    pre => {
          |        routeScopeStart( $RouteFrame )
          |        routeFsmStepAssert( $RouteFrame, 1, 101, 1111 )
          |        routeFsmStepAssert( $RouteFrame, 2, 101, 1111 )
          |        routeScopeCommit( $RouteFrame )
          |    }
          |  }
          | }
          |}""".stripMargin

    test(source, over_200_200, {
      (name, result) =>
        name match {
          case CubeFrame =>
            val found = result.rowSet.map {
              row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong, row.cells(3) asLong)
            } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
            val expected = Array(
              (1,1,101,1111), (2,1,101,1111), (3,1,101,1111), (4,1,101,1111), (5,1,101,1111), (6,1,101,1111), (7,1,101,1111), (8,1,101,1111), (9,1,101,1111), (10,1,101,1111), (11,1,101,1111), (12,1,101,1111), (13,1,101,1111), (14,1,101,1111), (15,1,101,1111), (16,1,101,1111), (17,1,101,1111), (18,1,101,1111), (19,1,101,1111), (20,1,101,1111), (21,1,101,1111), (22,1,101,1111), (23,1,101,1111), (24,1,101,1111), (25,1,101,1111)
            )

            found should equal(expected)
          case _ =>
        }
    }
    )
  }

}
