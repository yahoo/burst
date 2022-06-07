/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.hydra.visits.{PhasedVisitSourceGenerator, SituVisitSourceGenerator}
import org.burstsys.eql.paths.VisitPath
import org.burstsys.eql.planning.visits.VisitLanes
import org.burstsys.motif.schema.model.MotifSchema
import org.burstsys.vitals.errors.VitalsException

import scala.language.postfixOps

object HydraVisitLanes {
  def apply(path: VisitPath, lanes: VisitLanes)(implicit context:GlobalContext, schema:MotifSchema): HydraVisitLanes = {
    val navigatorId = path.getNavigatorId

    val navigatorDecl = context.getDeclaration(navigatorId) match {
      case frameDecl: NavigatingDeclaration => frameDecl
      case _ =>
        throw new IllegalArgumentException(s"$navigatorId does not have a declaration")
    }
    // look up declaration for root
    navigatorDecl.selectHydraVisitLaneType(path) match {
      case SITU =>
        new HydraSituVisitLanes(path, lanes)
      case PHASED =>
        new HydraPhasedVisitLanes(path, lanes)
      case x: VisitType =>
        throw VitalsException(s"unexpected visit type '$x''")
    }
  }

  sealed case class VisitType(i: Int)
  object SITU extends VisitType(1)
  object PHASED extends VisitType(2)
}

trait HydraVisitLanes extends VisitSourceGenerator {
  protected def lanes: Array[_ <: HydraLaneSourceActions]

  def traverseLanes: Iterator[_ <: HydraLaneSourceActions] = lanes.sortBy(_.name.ordinal).reverseIterator
  override def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[Declaration] = {
    lanes.flatMap(_ getDeclarations scope).groupBy(_.name).values.map(_.head).toArray
  }
}

class HydraSituVisitLanes(val path: VisitPath, analysisLanes: VisitLanes)
  extends HydraVisitLanes with SituVisitSourceGenerator {
  override protected val lanes: Array[HydraSituLaneSourceActions] =
    analysisLanes.laneMap.map(v => new HydraSituLaneSourceActions(v._1, path, v._2)).toArray
}

class HydraPhasedVisitLanes(val path: VisitPath, analysisLanes: VisitLanes)
  extends HydraVisitLanes with PhasedVisitSourceGenerator {
  override protected val lanes: Array[HydraPhasedLaneSourceActions] =
    analysisLanes.laneMap.map(v => new HydraPhasedLaneSourceActions(v._1, path, v._2)).toArray
}

