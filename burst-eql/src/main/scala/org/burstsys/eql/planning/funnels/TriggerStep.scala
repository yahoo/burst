/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.funnels

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.{ControlExpression, QueryAction}
import org.burstsys.eql.generators.ActionPhase
import org.burstsys.eql.generators.ActionPhase.{ActionPhase, Post}
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.planning._
import org.burstsys.motif.motif.tree.eql.funnels.TriggeredStepDefinition
import org.burstsys.motif.motif.tree.values.ValueExpression

class TriggerStep (val funnelName: String, val treeStep: TriggeredStepDefinition)
  extends Step(treeStep)  with QueryAction
{
  def this(triggerStep: TriggerStep) = {
    this(triggerStep.funnelName, triggerStep.treeStep)
  }

  var getTimingExpression: ValueExpression = treeStep.getTimingExpression

  def getWithinValue: Long = treeStep.getWithinValue

  def getAfterValue: Long = treeStep.getAfterValue

  override def transformParameterReferences(parameters: ParameterMap): Unit = {
    getTimingExpression = transformToParameterReferences(getTimingExpression, parameters)
    controls = controls.map(ce => ControlExpression(transformToParameterReferences(ce.expression, parameters)))
  }

  override def phase(): ActionPhase = {
    controls.map(_.phase()).reduce((a:ActionPhase, b:ActionPhase) => if (a == Post) Post else b)
  }


  // a planned trigger step doesn't have enough information to generate code
  override def generateSource()(implicit context: GlobalContext): CodeBlock = throw new NotImplementedError()
}


