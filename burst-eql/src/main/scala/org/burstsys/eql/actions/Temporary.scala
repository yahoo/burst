/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.{TemporaryAggregateExpression, TemporaryFrequencyExpression}
import org.burstsys.eql.generators.Var
import org.burstsys.eql.planning._
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.motif.tree.values.{AggregationValueExpression, ValueExpression}

import scala.collection.mutable.ArrayBuffer

trait Temporary extends QueryAction  {
  def tempVar: Var
  def name: String
}

trait TemporaryExpression extends Temporary with ValueExpression with EqlExpression {
  def value: ValueExpression
}


object Temporary {
  def apply(e: AggregationValueExpression)(implicit global: GlobalContext): TemporaryExpression = TemporaryAggregateExpression(global.temporaryName, e)

  def placeTemporaries(e: Expression)(implicit global: GlobalContext): Array[TemporaryExpression] = {
    val temporaries: ArrayBuffer[TemporaryExpression] = new ArrayBuffer()
    transformToTemporaries(e, temporaries)
    temporaries.toArray
  }

  def transformToTemporaries(e: EqlExpression, temporaries: ArrayBuffer[TemporaryExpression])
                            (implicit global: GlobalContext): EqlExpression = {
    e.transformTree { n =>
      n.self match {
        case a: AggregationValueExpression =>
          val t = Temporary(a)
          temporaries += t
          t
        case tf: TemporaryExpression =>
          temporaries += tf
          tf
        case d =>
          n
      }
    }
  }
}














