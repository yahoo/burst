/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.funnels

import org.burstsys.eql._
import org.burstsys.eql.actions.Temporary
import org.burstsys.eql.parsing.ParsedFunnel
import org.burstsys.eql.planning.{ParameterMap, ParameterReference}
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.eql.funnels.{FunnelMatchDefinition, TriggeredStepDefinition}
import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.funnels.FunnelPathBase

import scala.collection.JavaConverters._
import scala.language.postfixOps

class FunnelImpl(tree: ParsedFunnel)(implicit global: GlobalContext) extends Funnel {

  private val name = tree.getName

  private[planning] val parameterRefs: Array[ParameterReference] = {
    tree.getParameters.map(p => ParameterReference(p))
  }
  private[planning] val parameterMap: ParameterMap = {
    parameterRefs.map(p => p.definition.getName ->p).toMap
  }

  implicit private[planning] val funnelVisits: Visits = new Visits(tree.getSchema)

  private[planning] val steps: List[TriggerStep] = tree.getTree.getSteps.asScala.flatMap {
    case t: TriggeredStepDefinition =>
      List(new TriggerStep(this.name, t))
  }.toList

  // Iterate through every step in the funnel
  steps.foreach { s =>
    val laneName = BasicLaneName(s"funnel($name)-${s.id}")
    // convert parameters to references
    s.transformParameterReferences(parameterMap)
    // calculate temporaries
    val funnelTemps = s.controls.flatMap(Temporary placeTemporaries _.expression)

    // Set initial basic actions in visits
    funnelTemps.foreach(_ placeInVisit laneName)
    s placeInVisit laneName
    s.controls.foreach(_ placeInVisit laneName)
  }

  override def getVisits: Visits = funnelVisits

  override def getSchemaName: String = tree.getSchemaName

  override def getSourceNames: List[String] = tree.getSources.map(_.getName)

  override def getStepLimit: Integer = tree.getStepLimit

  override def getPathLimit: Integer = if (tree.isRepeating) tree.getStepLimit else 1

  override def getWithin: Long = tree.getTree.getWithinValue

  override val getParameters: Array[ParameterReference] = parameterRefs

  override def getSteps: Seq[TriggerStep] = steps

  override def getName: String = this.name

  override def getMatchDefinition: FunnelMatchDefinition = tree.getTree.getDefinition

  override def getTags: Array[String] = tree.getTags

  override def formPath(pathString: String): Path = FunnelPathBase.formPath(tree.getTree, pathString)
}
