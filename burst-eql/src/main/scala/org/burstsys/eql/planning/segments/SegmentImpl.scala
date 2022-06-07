/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.segments

import org.burstsys.eql._
import org.burstsys.eql.actions.Temporary
import org.burstsys.eql.parsing.ParsedSegment
import org.burstsys.eql.planning.{ParameterMap, ParameterReference}
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.visits.Visits

import scala.collection.JavaConverters._
import scala.language.postfixOps

class SegmentImpl(tree: ParsedSegment)(implicit global: GlobalContext) extends Segment {

  private val name = tree.getName

  private[planning] val parameterRefs: Array[ParameterReference] = {
    tree.getParameters.map(p => ParameterReference(p))
  }
  private[planning] val parameterMap: ParameterMap = {
    parameterRefs.map(p => p.definition.getName ->p).toMap
  }

  implicit private[planning] val segmentVisits: Visits = new Visits(tree.getSchema)

  private[planning] val definitions: List[Definition] = tree.getTree.getDefinitions.asScala
    .map(new Definition(this.name, _)).toList

  // Iterate through every step in the funnel
  definitions.foreach { d =>
    val laneName = BasicLaneName(s"segment($name)-${d.id}")
    // convert parameters to references for substitution
    d.transformParameterReferences(parameterMap)
    // calculate temporaries
    val funnelTemps = d.controls.flatMap(Temporary placeTemporaries _.expression)

    // Set initial basic actions in visits
    funnelTemps.foreach(_ placeInVisit laneName)
    d placeInVisit laneName
    d.controls.foreach(_ placeInVisit laneName)
  }

  override def getVisits: Visits = segmentVisits

  override def getSchemaName: String = tree.getSchemaName

  override def getSourceNames: List[String] = tree.getSources.map(_.getName)

  override val getParameters: Array[ParameterReference] = parameterRefs

  override def getDefinitions: Seq[Definition] = definitions

  override def getName: String = this.name
}
