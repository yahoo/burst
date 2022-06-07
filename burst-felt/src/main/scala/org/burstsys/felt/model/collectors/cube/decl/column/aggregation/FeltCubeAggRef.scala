/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColRef
import org.burstsys.felt.model.collectors.cube.generate.{FeltStaticCubeSpace, cubeBuilderVariable, cubeDictionary}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.sweep.symbols.{brioPrimitiveClass, sweepRuntimeSym}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}

import scala.language.postfixOps

object FeltCubeAggRef {

  final case class FeltCubeAggRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltCubeAggDecl] {
    override val resolverName: String = "cube.agg"

    override protected
    def addResolution(refName: FeltPathExpr, d: FeltCubeAggDecl): FeltReference =
      FeltCubeAggRef(refName, d)

    override protected
    def addNomination(c: FeltCubeAggDecl): Option[FeltReference] =
      Some(FeltCubeAggRef(c.refName, c))

  }

}


final case
class FeltCubeAggRef(refName: FeltPathExpr, refDecl: FeltCubeAggDecl) extends FeltCubeColRef {

  sync(refDecl)

  override val nodeName: String = "felt-cube-agg-ref"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode = {
    generateAggregationWrite
  }

  private
  def generateAggregationWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    val ns = FeltStaticCubeSpace(global, cubeName)
    s"""|
        |${T(this, s"agg-write")}
        |${I}if(${cursor.callScope.scopeNull}) {
        |$I2${ns.currentInstCube}.writeAggregationNull(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal) // $relationName
        |$I} else {
        |${I2}val semantic = ${cubeBuilderVariable(cubeName)}.aggregationSemantics($relationOrdinal); // $relationName
        |${I2}val wasNull = ${ns.currentInstCube}.readAggregationNull(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal); // $relationName
        |${I2}val oldValue: Long = if (wasNull) {
        |${I3}$aggregationInit
        |$I2} else {
        |$I3${ns.currentInstCube}.readAggregationPrimitive(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal) // $relationName
        |$I2}
        |${I2}val newValue = semantic.$aggregationUpdate;
        |$I2${ns.currentInstCube}.writeAggregationPrimitive(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal, newValue) // $relationName
        |$I}""".stripMargin
  }


  private
  def aggregationInit(implicit cursor: FeltCodeCursor): FeltCode = {
    relationType match {
      case BrioBooleanKey => s"semantic.doBooleanInit();"
      case BrioByteKey => s"semantic.doByteInit();"
      case BrioShortKey => s"semantic.doShortInit();"
      case BrioIntegerKey => s"semantic.doIntegerInit();"
      case BrioLongKey => s"semantic.doLongInit();"
      case BrioDoubleKey => s"semantic.doDoubleInit();"
      case BrioStringKey => s"// strings don't init..."
    }
  }

  private
  def aggregationUpdate(implicit cursor: FeltCodeCursor): FeltCode = {
    val s =
    relationType match {
      case BrioBooleanKey => s"doBoolean(oldValue, ${cursor.callScope.scopeVal})"
      case BrioByteKey => s"doByte(oldValue, ${cursor.callScope.scopeVal})"
      case BrioShortKey => s"doShort(oldValue, ${cursor.callScope.scopeVal})"
      case BrioIntegerKey => s"doInteger(oldValue, ${cursor.callScope.scopeVal})"
      case BrioLongKey => s"doLong(oldValue, ${cursor.callScope.scopeVal})"
      case BrioDoubleKey =>
        s"doDouble(oldValue, $brioPrimitiveClass.brioDoubleToPrimitive( ${cursor.callScope.scopeVal} ) ); "
      case BrioStringKey =>
        if (cursor.global.lexicon.enabled)
          s"doLexiconString(${sweepRuntimeSym}.dictionary, ${sweepRuntimeSym}.${cubeDictionary(cubeName)}, oldValue, ${cursor.callScope.scopeVal})"
        else
          s"doString(${sweepRuntimeSym}.${cubeDictionary(cubeName)}, oldValue, ${cursor.callScope.scopeVal})"
    }
    s
  }

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
