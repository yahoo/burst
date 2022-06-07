/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColRef
import org.burstsys.felt.model.collectors.cube.generate.{FeltStaticCubeSpace, cubeBuilderVariable, cubeDictionary}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}

import scala.language.postfixOps

object FeltCubeDimRef {

  final case class FeltCubeDimRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltCubeDimDecl] {
    override val resolverName: String = "cube.dim"

    override protected
    def addResolution(refName: FeltPathExpr, d: FeltCubeDimDecl): FeltReference =
      FeltCubeDimRef(refName, d)

    override protected
    def addNomination(c: FeltCubeDimDecl): Option[FeltReference] =
      Some(FeltCubeDimRef(c.refName, c))

  }

}


final case
class FeltCubeDimRef(refName: FeltPathExpr, refDecl: FeltCubeDimDecl) extends FeltCubeColRef {

  sync(refDecl)

  override val nodeName: String = "felt-cube-dim-ref"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode = {
    generateDimensionWrite
  }

  private
  def generateDimensionWrite(implicit cursor: FeltCodeCursor): FeltCode = {
    val ns = FeltStaticCubeSpace(global, cubeName)
    s"""|
        |${T(this, s"dim-write")}
        |${I}if(${cursor.callScope.scopeNull}) {
        |$I2${ns.currentInstCube}.writeDimensionNull(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal) // $relationName
        |$I} else {
        |${I2}val semantic = ${cubeBuilderVariable(cubeName)}.dimensionSemantics($relationOrdinal); // $relationName
        |${I2}val newValue = semantic.$dimensionUpdate(rt);
        |$I2${ns.currentInstCube}.writeDimensionPrimitive(${ns.cubeBuilderVar}, ${ns.currentInstCube}, $relationOrdinal, newValue) // $relationName
        |$I}""".stripMargin
  }

  private
  def dimensionUpdate(implicit cursor: FeltCodeCursor): FeltCode = {
    relationType match {
      case BrioBooleanKey => s"doBoolean(${cursor.callScope.scopeVal})"
      case BrioByteKey => s"doByte(${cursor.callScope.scopeVal})"
      case BrioShortKey => s"doShort(${cursor.callScope.scopeVal})"
      case BrioIntegerKey => s"doInteger(${cursor.callScope.scopeVal})"
      case BrioLongKey => s"doLong(${cursor.callScope.scopeVal})"
      case BrioDoubleKey => s"doDouble(${cursor.callScope.scopeVal})"
      case BrioStringKey =>
        if (cursor.global.lexicon.enabled)
          s"doLexiconString(${sweepRuntimeSym}.dictionary, ${sweepRuntimeSym}.${cubeDictionary(cubeName)}, ${cursor.callScope.scopeVal})"
        else
          s"doString(${sweepRuntimeSym}.${cubeDictionary(cubeName)}, ${cursor.callScope.scopeVal})"
    }
  }

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
