/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.surgeon

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.sweep.symbols.{controlRelationScopeValue, feltSchemaSym, releaseCubeMethod, vitalsBitMapClass}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3, I4}

trait FeltCubeStatMergeSurgeon {

  /**
   * generalized merge of a child path into a parent path.
   *
   * @param cube
   * @param cursor
   * @return
   */
  final
  def genStatChildMerge(cube: FeltCubeDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
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

}
