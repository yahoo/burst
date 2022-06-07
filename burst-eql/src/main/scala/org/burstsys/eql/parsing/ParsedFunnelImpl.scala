/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.parsing

import org.burstsys.eql._
import org.burstsys.motif.motif.tree.eql.common.Source
import org.burstsys.motif.motif.tree.eql.funnels.Funnel
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.schema.model.MotifSchema

import scala.collection.JavaConverters._

class ParsedFunnelImpl(val funnel: Funnel)(implicit globalContext: GlobalContext) extends ParsedFunnel {

  private val gLimit: Integer = if (funnel.getLimit != null && funnel.getLimit > 0)
    funnel.getLimit
  else
    new Integer(100)

  override def getSchema: MotifSchema = funnel.getSchema

  def getSchemaName: String = getSchema.getSchemaName

  def getSources: List[Source] = funnel.getSources.asScala.toList

  override def getTree: Funnel = funnel

  override def getStepLimit: Integer = gLimit

  override def isRepeating: Boolean = funnel.getType == Funnel.Type.TRANSACTION

  override def getParameters: Array[ParameterDefinition] = funnel.getParameters.asScala.toArray

  override def getTags: Array[String] = funnel.getTags.asScala.toArray

  override def getName: String = funnel.getName
}
