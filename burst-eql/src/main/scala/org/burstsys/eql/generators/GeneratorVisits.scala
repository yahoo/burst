/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.paths.VisitPath
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.schema.model.MotifSchema

import scala.collection.mutable

final case class GeneratorVisits(visits: Visits)(implicit context: GlobalContext) extends SourceGenerator {
  private val hydraMap = mutable.Map[VisitPath, HydraVisitLanes]()
  implicit val schema: MotifSchema = visits.schema

  for ((visitPath, visitSteps) <- visits.visitMap) {
    hydraMap.getOrElseUpdate(visitPath, HydraVisitLanes(visitPath, visitSteps))
  }

  override def generateSource()(implicit context: GlobalContext): CodeBlock = {
    CodeBlock { implicit cb =>
      hydraMap.foreach {
        case (_, hydraVisit) =>
              hydraVisit.generateSource().source()
      }
    }
  }

  def geneterateRootPost(): Iterator[CodeBlock] = {
    if (hydraMap.contains(visits.getSchemaRoot)) {
      hydraMap(visits.getSchemaRoot).traverseLanes.map(_.generateSource(ActionPhase.Post)).filter(_.nonEmpty)
    } else {
      Iterator.empty
    }
  }

  override def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[Declaration] = {
    hydraMap.values.flatMap(_.getDeclarations(scope)).toArray
  }
}
