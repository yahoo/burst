/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.parsing

import org.burstsys.eql._
import org.burstsys.motif.motif.tree.eql.common.Source
import org.burstsys.motif.motif.tree.eql.segments.Segment
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.schema.model.MotifSchema

import scala.collection.JavaConverters._

class ParsedSegmentImpl(val segment: Segment)(implicit globalContext: GlobalContext) extends ParsedSegment {

  override def getSchema: MotifSchema = segment.getSchema

  override def getSchemaName: String = segment.getSchemaName

  override def getSources: List[Source] = segment.getSources.asScala.toList

  override def getTree: Segment = segment

  override def getParameters: Array[ParameterDefinition] = segment.getParameters.asScala.toArray

  override def getName: String = segment.getName
}
