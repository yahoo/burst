/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql.{GlobalContext, qualifiedFrameName, qualifiedName}
import org.burstsys.eql.actions.{Dimension, DimensionSource, VerbatimDimensionAssign}
import org.burstsys.eql.actions.temporaries.TemporaryFrequencyExpression
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.utils._


trait TemporaryFrequencyReinitializeSourceGenerator extends ActionSourceGenerator with DimensionSource {
  self: TemporaryFrequencyExpression =>

  def aggTemporary: Var
  def holdTemporary: Var

  def getDimensions: Array[Dimension] = {
    Array(
      VerbatimDimensionAssign(dimensionTargetName, dimension),
      surrounding
    )
  }

  // this generator controls the dimension inserts in it's visit
  override def providesDimensionWrite: Boolean = true


  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val items = dimension.generateSource()
    val surround = surrounding.expression.generateSource.head
    assert(items.length == 1)
    CodeBlock{ implicit cb: CodeBlock =>
      s"""
         |${holdTemporary.name} = ${items.head}
         |if ($name == null) {
         |  $name=${holdTemporary.name}
         |} else if ($name != ${holdTemporary.name}) {
         |""".stripMargin.source
      CodeBlock { implicit cb =>
        s"${qualifiedName(dimensionTargetName)} = $name".source()
        s"${qualifiedName(frequencyTargetName)} = $surround".source()
        s"insert($qualifiedFrameName)".source
        s"${aggTemporary.name}= 0".source
        s"$name=${holdTemporary.name}".source
      }.indent.source
      "}".source
    }
  }
}
