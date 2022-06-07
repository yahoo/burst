/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityRoute00Query extends HydraUseCase(200, 200, "unity") {

  //    override val sweep = new B5BA9EA413EE045BA9D218242ED1A0F4D

  //  override val serializeTraversal = true

  override def frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 9999
       |    aggregates {
       |      a0:sum[long]
       |    }
       |    dimensions {
       |      d0:verbatim[long]
       |    }
       |  }
       |    user.sessions.r1.paths ⇒ {
       |    before ⇒ {
       |      // before all paths
       |    }
       |    pre ⇒ {
       |      // before each path's children
       |    }
       |    post ⇒ {
       |      // after each path's children
       |    }
       |    after ⇒ {
       |      // after all paths
       |    }
       |  }
       |
       |  user.sessions.r1.paths.steps ⇒ {
       |    before ⇒ {
       |      // before all steps
       |    }
       |    situ ⇒ {
       |      // for each step
       |    }
       |    after ⇒ {
       |      // after all steps
       |    }
       |  }
       |
       |  user.sessions.r1.courses ⇒ {
       |    before ⇒ {
       |      // before all courses
       |    }
       |    situ ⇒ {
       |      // for each course
       |    }
       |    after ⇒ {
       |      // after all courses
       |    }
       |  }
       |
       |}
       |
       |frame frame2 {
       |  route user.sessions {
       |    graph {
       |      enter 1 {
       |          to(2)
       |          to(3, 0, 0)
       |      }
       |      exit 2 {
       |
       |      }
       |      exit 3 {
       |
       |      }
       |    }
       |  }
       |
       |  user.sessions ⇒ {
       |  }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
