/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.surgeon

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.sweep.symbols.{controlRelationScopeValue, feltSchemaSym, releaseCubeMethod, vitalsBitMapClass}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I1, I2, I3, I4}

trait FeltCubeDynMergeSurgery {

  private
  val bitMapClass = vitalsBitMapClass

  /**
   * generate a join of the dynamic (extended) relationship into the parent (static) instance
   *
   * @param cube
   * @param cursor
   * @return
   */
  final
  def genDynRelMerge(cube: FeltCubeDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
    val global = cube.global
    val calculatedCube = cube.calculus.childMergeAt(cursor.pathKey).get.cube
    val dimensionMask = calculatedCube.dimensionMask.data
    val aggregationMask = calculatedCube.aggregationMask.data
    val cubeName = cube.cubeName
    val space = FeltStaticCubeSpace(global, cubeName)

    val dictionary = space.cubeDictionaryVar
    val builder = space.cubeBuilderVar
    val currentRelation = space.currentRelCube
    val parentInstance = space.parentInstCube

    val controlAbortCode: FeltCode = if (!cube.global.features.ctrlVerbs) true.toString
    else
      s"""|
          |${C(s"control verb abort (clear) of a relation and all its children")}
          |${I2}if ( $feltSchemaSym.visitInScope(path, ${controlRelationScopeValue(cubeName)} ) ) $currentRelation.clear(); """.stripMargin

    s"""|
        |${I}if ( $currentRelation != null ) { $controlAbortCode
        |${I2}if ( !$currentRelation.isEmpty ) {
        |$I3$parentInstance.intraMerge(
        |$I4$builder,
        |$I4$parentInstance, // this cube (destination)
        |$I4$dictionary,
        |$I4$currentRelation,   // that cube (source)
        |$I4$dictionary,
        |$I4$vitalsBitMapClass( $dimensionMask ),  $vitalsBitMapClass( $aggregationMask )
        |$I3);
        |$I2}
        |$I2$releaseCubeMethod( $currentRelation );
        |$I2$currentRelation = null;
        |$I}""".stripMargin
  }

  /**
   * merge a single dynamic instance/member in an iteration visit into the dynamic (schema extended) relation
   *
   * @param cube
   * @param cursor
   * @return
   */
  final
  def genDynInstMerge(cube: FeltCubeDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
    val global = cube.global
    val cubeName = cube.cubeName

    val calculus = cube.calculus

    val pathKey = cursor.pathKey

    val mergeCube =
      if (calculus.isChildMergeAt(pathKey)) {
        calculus.childMergeAt(pathKey).getOrElse(
          throw FeltException(cube, s"FELT_CUBE_SURGERY_NO_MERGE! ${cursor.pathName}")
        ).cube
      } else if (calculus.isChildJoinAt(pathKey)) {
        val maybeJoin = calculus.childJoinAt(pathKey)
        maybeJoin.getOrElse(
          throw FeltException(cube, s"FELT_CUBE_SURGERY_NO_MERGE! ${cursor.pathName}")
        ).childCube
      } else
        throw FeltException(cube, s"FELT_CUBE_SURGERY_NO_MERGE_OR_JOIN! ${cursor.pathName}")

    val dimensionMask = mergeCube.dimensionMask.data
    val aggregationMask = mergeCube.aggregationMask.data

    val space = FeltStaticCubeSpace(global, cubeName)
    val builder = space.cubeBuilderVar
    val dictionary = space.cubeDictionaryVar
    val currentInstance = space.currentInstCube
    val currentRelation = space.currentRelCube

    s"""|
        |${C("merge the dynamic instance/member into the dynamic/extended relation")}
        |${I1}if ( !$currentInstance.isEmpty ) {
        |$I2$currentRelation.intraMerge(
        |$I3$builder,
        |$I3$currentRelation, // this cube (destination)
        |$I3$dictionary,
        |$I3$currentInstance,  // that cube (source)
        |$I3$dictionary,
        |$I3$bitMapClass( $dimensionMask ),
        |$I3$bitMapClass( $aggregationMask )
        |$I2);
        |${C("clear the instance/member cube to be ready for the next iteration")}
        |${I}$currentInstance.clear();
        |$I1}  """.stripMargin
  }

}
