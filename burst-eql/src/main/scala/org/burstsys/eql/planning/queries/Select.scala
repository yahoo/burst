/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.queries

import org.burstsys.eql.actions._
import org.burstsys.eql.actions.temporaries.{TemporaryExpressionContext, TemporaryFrequencyExpression}
import org.burstsys.eql.planning.escapeIdentifierName
import org.burstsys.eql.planning.lanes.LaneControl._
import org.burstsys.motif.motif.tree.eql.queries
import org.burstsys.motif.motif.tree.eql.queries.Target
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.motif.tree.values._
import org.burstsys.motif.symbols.functions.FrequencyFunction.FrequencyContext
import org.burstsys.motif.symbols.functions.Functions

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer

final case class Select (name: String,
                         aggregates: Array[Aggregate],
                         dimensions: Array[DimensionAssign],
                         controls: Array[ControlExpression],
                         specials: Array[TemporaryExpression],
                         limit: Integer
                        )

object Select {
  def apply(treeSelect: queries.Select): Select = {
    val aggs = ArrayBuffer[Aggregate]()
    val dims = ArrayBuffer[DimensionAssign]()
    val specials = ArrayBuffer[TemporaryExpression]()

    // classify the targets into aggregates and dimensions
    for (x: Target <- treeSelect.getTargets.asScala) {
      val name = escapeIdentifierName(x.getName)
      val dim = x.getExpression match {
        case a: AggregationValueExpression =>
          // true user aggregates can only be unqualified and at the root level
          if (a.getWhere == null && a.hasDefaultQuanta && a.atRootScope) {
            aggs += Aggregate(name, a)
            null
          } else
            VerbatimDimensionAssign(name, a)
        case dq: DateTimeQuantumExpression =>
          QuantaDimensionAssign(name, dq.getOp, dq.getExpr)
        case od: DateTimeOrdinalExpression =>
          OrdinalDimensionAssign(name, od.getOp, od.getExpr)
        case ff: FunctionExpression if ff.getFunctionName == Functions.SPLIT =>
          val parms = ff.getParms.asScala
          SplitDimensionAssign(name, parms.head, parms.drop(1).toList)
        case ff: FunctionExpression if ff.getFunctionName == Functions.ENUM =>
          val parms = ff.getParms.asScala
          EnumDimensionAssign(name, parms.head, parms.drop(1).toList)
        case e: Expression =>
          VerbatimDimensionAssign(name, e)
      }
      if (dim != null) {
        // dimensions might be taken over by special hoisted functions
        val fa = hoistSpecials(x, dim)
        if (fa == null)
          dims += dim
        else {
          specials += fa
        }
      }
    }

    // break out the where clause into high level AND clauses
    val ctls = extractAndControls(treeSelect.getWhere)

    new Select(treeSelect.getName, aggs.toArray, dims.toArray, ctls, specials.toArray, treeSelect.getLimit)
  }

  private def hoistSpecials(t: Target, da: DimensionAssign): TemporaryExpression = {
    var freq: TemporaryFrequencyExpression = null
    val surround = da.transformTree { n =>
      n.self match {
        case ff: FunctionExpression if ff.getFunctionName == Functions.FREQUENCY =>
          val context = ff.getContext.asInstanceOf[FrequencyContext]
          freq = TemporaryFrequencyExpression(
            dimensionTargetName = t.getName,
            frequencyTargetName = t.getName + "_frequency",
            scope = context.scopePath,
            value = context.frequencyPath,
            dimension = context.dimensionExpression,
            surrounding = null
          )
          new TemporaryExpressionContext(freq.name, freq)
        case _ => n
      }
    }
    if (freq != null) {
      freq.surrounding = surround.asInstanceOf[DimensionAssign].copy(freq.frequencyTargetName)
      freq
    } else
      null
  }
}
