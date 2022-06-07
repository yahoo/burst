/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.brio.types.BrioTypes.{BrioShortKey, BrioStringKey, scalaTypeNameFromBrioTypeKey}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2}
import org.burstsys.vitals.strings.VitalsGeneratingTraversable

/**
 * These are code generated name templates placed here so they are consistent throughout the final output
 * to the scala compiler compile phase
 */
trait FeltLatSymbols {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT LATTICE SCALAR/VECTOR REFERENCE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def latticeRelation(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_relation"

  final
  def latticeRelationViaSchemaRuntime(path: BrioPathName): FeltSchemaVar = s"$schemaRuntimeSym.${latticeRelation(path)}"

  final
  def latticeRelationViaSweepRuntime(path: BrioPathName): FeltSweepVar = s"$sweepRuntimeSym.${latticeRelation(path)}"

  final
  def latticeRelationIsNull(path: BrioPathName): FeltVar = s"lattice_${normalizedPathName(path)}_relation_isNull"

  final
  def latticeRelationIsNullViaSchemaRuntime(path: BrioPathName): FeltSchemaVar = s"$schemaRuntimeSym.${latticeRelationIsNull(path)}"

  final
  def latticeRelationIsNullViaSweepRuntime(path: BrioPathName): FeltSweepVar = s"$sweepRuntimeSym.${latticeRelationIsNull(path)}"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT LATTICE VECTOR LAST/FIRST
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def latticeVectorIsFirstViaSchemaRuntime(path: BrioPathName): FeltSchemaVar = s"$schemaRuntimeSym.${latticeVectorIsFirst(path)}"

  final
  def latticeVectorIsFirstViaSweepRuntime(path: BrioPathName): FeltSweepVar = s"$sweepRuntimeSym.${latticeVectorIsFirst(path)}"

  final
  def latticeVectorIsFirst(path: BrioPathName): FeltVar = s"lattice_${normalizedPathName(path)}_vector_is_first"

  final
  def latticeVectorIsLastViaSchemaRuntime(path: BrioPathName): FeltSchemaVar = s"$schemaRuntimeSym.${latticeVectorIsLast(path)}"

  final
  def latticeVectorIsLastViaSweepRuntime(path: BrioPathName): FeltSweepVar = s"$sweepRuntimeSym.${latticeVectorIsLast(path)}"

  final
  def latticeVectorIsLast(path: BrioPathName): FeltVar = s"lattice_${normalizedPathName(path)}_vector_is_last"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT LATTICE VALUE MAP
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def latticeValueMapKey(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_map_key"

  final
  def latticeValueMapKeyViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueMapKey(path)}"

  final
  def latticeValueMapKeyViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueMapKey(path)}"

  /*
  final
  def latticeValueMapKeyIsNull(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_map_key_is_null"
*/

  /*
  final
  def latticeValueMapKeyIsNullViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueMapKeyIsNull(path)}"

  final
  def latticeValueMapKeyIsNullViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueMapKeyIsNull(path)}"
*/

  final
  def latticeValueMapValue(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_map_value"

  final
  def latticeValueMapValueViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueMapValue(path)}"

  final
  def latticeValueMapValueViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueMapValue(path)}"

  /*
  final
  def latticeValueMapValueIsNull(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_map_value_is_null"
*/

  /*
  final
  def latticeValueMapValueIsNullViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueMapValueIsNull(path)}"

  final
  def latticeValueMapValueIsNullViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueMapValueIsNull(path)}"
*/

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT LATTICE VALUE VECTOR
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def latticeValueVectorValue(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_vector_value"

  final
  def latticeValueVectorValueViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueVectorValue(path)}"

  final
  def latticeValueVectorValueViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueVectorValue(path)}"

  final
  def latticeValueVectorValueIsNull(path: BrioPathName) = s"lattice_${normalizedPathName(path)}_value_vector_value_is_null"

  final
  def latticeValueVectorValueIsNullViaSweepRuntime(path: BrioPathName) = s"$sweepRuntimeSym.${latticeValueVectorValueIsNull(path)}"

  final
  def latticeValueVectorValueIsNullViaSchemaRuntime(path: BrioPathName) = s"$schemaRuntimeSym.${latticeValueVectorValueIsNull(path)}"

  final val latticeVarInitialize = s"latticeVarInitialize"

  final
  def generateLatticeVarInitializeCall(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I$latticeVarInitialize($schemaRuntimeSym);""".stripMargin
  }

  /**
   * generate the appropriate lattice variables into the [[FeltRuntime]]
   *
   * @param cursor
   * @return
   */
  final
  def generateLatticeVarDeclarations(implicit cursor: FeltCodeCursor): FeltCode = {
    def pathCode(implicit cursor: FeltCodeCursor): FeltCode = {
      for (i <- 1 to cursor.schema.brioSchema.pathCount) yield {
        val node = cursor.schema.brioSchema.nodeForPathKey(i)
        val keyScalaTypeName = {
          val keyType = node.relation.keyEncoding.typeKey
          if (cursor.global.lexicon.enabled && keyType == BrioStringKey)
            scalaTypeNameFromBrioTypeKey(BrioShortKey)
          else
            scalaTypeNameFromBrioTypeKey(keyType)
        }
        val valueScalaTypeName = {
          val valueType = node.relation.valueEncoding.typeKey
          if (cursor.global.lexicon.enabled && valueType == BrioStringKey)
            scalaTypeNameFromBrioTypeKey(BrioShortKey)
          else
            scalaTypeNameFromBrioTypeKey(valueType)
        }
        val pathName = node.pathName
        node.relation.relationForm match {
          case BrioReferenceScalarRelation =>
            s"""|
                |${I}final var ${latticeRelation(pathName)}:$brioLatticeReferenceClass = $brioLatticeReferenceAnyValClass();
                |${I}final var ${latticeRelationIsNull(pathName)}:Boolean = true;""".stripMargin
          case BrioReferenceVectorRelation =>
            s"""|
                |${I}final var ${latticeRelation(pathName)}:$brioLatticeReferenceClass = $brioLatticeReferenceAnyValClass();
                |${I}final var ${latticeRelationIsNull(pathName)}:Boolean = true;
                |${I}final var ${latticeVectorIsFirst(pathName)}:Boolean = false;
                |${I}final var ${latticeVectorIsLast(pathName)}:Boolean = false;""".stripMargin
          case BrioValueMapRelation =>
            s"""|
                |${I}final var ${latticeRelationIsNull(pathName)}:Boolean = true;
                |${I}final var ${latticeValueMapKey(pathName)}:$keyScalaTypeName = _ ;
                |${I}final var ${latticeValueMapValue(pathName)}:$valueScalaTypeName = _ ;
                |${I}final var ${latticeVectorIsFirst(pathName)}:Boolean = false;
                |${I}final var ${latticeVectorIsLast(pathName)}:Boolean = false;""".stripMargin
          case BrioValueVectorRelation =>
            s"""|
                |${I}final var ${latticeRelationIsNull(pathName)}:Boolean = true;
                |${I}final var ${latticeValueVectorValue(pathName)}:$valueScalaTypeName = _ ;
                |${I}final var ${latticeValueVectorValueIsNull(pathName)}:Boolean = true;
                |${I}final var ${latticeVectorIsFirst(pathName)}:Boolean = false;
                |${I}final var ${latticeVectorIsLast(pathName)}:Boolean = false;""".stripMargin
          case _ => null
        }
      }.mkString
    }.noNulls.mkString

    s"""|
        |${C("lattice variable declarations")}
        |$pathCode""".stripMargin
  }

  /**
   * initialize lattice variable access points
   *
   * @param cursor
   * @return
   */
  final
  def generateLatticeVarInitializations(brioSchema: BrioSchema, runtimeClassName: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C("lattice variable initialization")}
        |$I@inline
        |${I}def $latticeVarInitialize($schemaRuntimeSym:$runtimeClassName): Unit = {
        |$I2${latticeRelationViaSchemaRuntime(brioSchema.rootNode.pathName)} = $blobLatticeSym; // init root of lattice...
        |$I2${latticeRelationIsNullViaSchemaRuntime(brioSchema.rootRelationName)} = false;
        |$I}""".stripMargin
  }

}
