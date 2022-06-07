/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions.temporaries

import java.util

import org.burstsys.eql.actions.DimensionAssign
import org.burstsys.eql.generators.hydra.temporaries._
import org.burstsys.eql.generators.{DeclarationScope, Var}
import org.burstsys.eql.planning.lanes.LaneName
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.common.DataType
import org.burstsys.motif.motif.tree.data.PathAccessor
import org.burstsys.motif.motif.tree.expression.{Evaluation, Expression}
import org.burstsys.motif.paths.Path
import org.burstsys.motif.symbols.PathSymbols

case class TemporaryFrequencyExpression(
                                                dimensionTargetName: String,
                                                frequencyTargetName: String,
                                                scope: Path,
                                                override val value: PathAccessor,
                                                var dimension: Expression,
                                                var surrounding: DimensionAssign)
  extends TemporaryExpressionContext("T_" + frequencyTargetName, value) {

  override val tempVar: Var = Var("T_" + frequencyTargetName, DeclarationScope.Frame, DataType.LONG)

  override def getLowestVisitPath: Path =
    Path.lowest(value.getLowestEvaluationPoint, dimension.getLowestEvaluationPoint)

  override def placeInVisit(lane: LaneName)(implicit visits: Visits): Unit = {
    val calculationPath = Path.lowest(value.getLowestEvaluationPoint, scope)

    val aggregate = this.tempVar
    val dimensionHold = Var(s"T_$dimensionTargetName", DeclarationScope.Frame, dimension.getDtype, nulled = true)
    val dimensionHoldTemp = Var(s"${dimensionHold.name}_T", DeclarationScope.Visit, dimension.getDtype, nulled = true)

    // place the visit temporary
    visits.addGenerator(lane)(calculationPath,
      new TemporaryFrequencyExpression(dimensionTargetName, frequencyTargetName, scope, value, dimension, surrounding)
        with TemporaryFrequencyTempDeclSourceGenerator   {
        override val tempVar: Var = dimensionHoldTemp
      })
    // place the frequency aggregate's initializations
    visits.addGenerator(lane)(scope,
      new TemporaryFrequencyExpression(dimensionTargetName, frequencyTargetName, scope, value, dimension, surrounding)
        with TemporaryFrequencyInitializerSourceGenerator  )

    // place the dimension change check
    visits.addGenerator(lane)(calculationPath,
      new TemporaryFrequencyExpression(dimensionTargetName, frequencyTargetName, scope, value, dimension, surrounding)
        with TemporaryFrequencyReinitializeSourceGenerator {
        def aggTemporary: Var = aggregate
        def holdTemporary: Var = dimensionHoldTemp
        override val tempVar: Var = dimensionHold
    })
    // place the frequency count
    visits.addGenerator(lane)(calculationPath,
      new TemporaryFrequencyExpression(dimensionTargetName, frequencyTargetName, scope, value, dimension, surrounding)
        with TemporaryFrequencySourceGenerator  )
    // and the frequency count final write
    visits.addGenerator(lane)(calculationPath,
      new TemporaryFrequencyExpression(dimensionTargetName, frequencyTargetName, scope, value, dimension, surrounding)
        with TemporaryFrequencyAfterSourceGenerator {
        def aggTemporary: Var = aggregate
        override val tempVar: Var = dimensionHold
      })
  }

  override def optimize(pathSymbols: PathSymbols): Evaluation = {
    if (this.canReduceToConstant)
      this.reduceToConstant
    else {
      this.dimension = dimension.optimize(pathSymbols).asInstanceOf[Expression]
      this
    }
  }

  override def getDtype: DataType = DataType.LONG

  override def getChildren: util.List[Expression] = util.Arrays.asList(value, dimension)

  override def childCount(): Int = 3

  override def getChild(index: Int): Expression = {
    if (index == 0)
      value
    else if (index == 1)
      dimension
    else if (index == 2)
      surrounding
    else
      throw new IndexOutOfBoundsException
  }

  override def setChild(index: Int, value: Expression): Expression = {
    if (index == 0) {
      // only allow setting to the same values
      if (value == this.value)
        value
      else
        throw new UnsupportedOperationException
    } else if (index == 1) {
      // only allow setting to the same values
      if (value == this.dimension)
        value
      else
        throw new UnsupportedOperationException
    } else if (index == 2) {
      // only allow setting to the same values
      this.surrounding = value.asInstanceOf
      value
    } else
      throw new IndexOutOfBoundsException
  }
}
