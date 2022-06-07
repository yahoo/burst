/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.surgeon

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3, I4}

trait FeltCubeStatJoinSurgery {

  /**
   * generalized join of a child path into a parent path
   *
   * @param cube
   * @param cursor
   * @return
   */
  final
  def genStatChildJoin(cube: FeltCubeDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
    val global = cube.global
    val childCalculus = cube.calculus.childJoinAt(cursor.pathKey).get.childCube
    val childDimensionMask = childCalculus.dimensionMask.data
    val childAggregationMask = childCalculus.aggregationMask.data

    val parentCalculus = cube.calculus.childJoinAt(cursor.pathKey).get.parentCube
    val parentDimensionMask = parentCalculus.dimensionMask.data
    val parentAggregationMask = parentCalculus.aggregationMask.data

    val cubeName = cube.cubeName
    val zcs = FeltStaticCubeSpace(global, cubeName)
    val binding = cube.global.binding.collectors.cubes

    val dictionary = zcs.cubeDictionaryVar
    val builder = zcs.cubeBuilderVar
    val currentRelation = zcs.currentRelCube
    val parentInstance = zcs.parentInstCube
    val currentInstance = zcs.currentInstCube
    val parentTmpInstance = zcs.parentInstTmpCube
    val collectorClass = binding.collectorClassName
    val bitMapClass = vitalsBitMapClass

    val controlAbortCode: FeltCode = if (!cube.global.features.ctrlVerbs) true.toString
    else
      s"""|
          |${C(s"control verb abort (clear) of a relation and all its children")}
          |${I2}if ( $feltSchemaSym.visitInScope(path, ${controlRelationScopeValue(cubeName)} ) ) $currentRelation.clear(); """.stripMargin

    s"""|
        |${I}if ( $currentRelation != null ) { $controlAbortCode
        |${I2}if ( !$currentRelation.isEmpty ) {
        |${I3}val $parentTmpInstance = $parentInstance;
        |${I3}val destinationCube = $grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ];
        |$I3$parentInstance.joinWithChildCubeIntoResultCube(
        |${I4}$builder,
        |${I4}$parentInstance, // this cube (left source)
        |${I4}$dictionary,
        |${I4}$currentRelation, // child cube (right source)
        |${I4}destinationCube,  // destination (result) cube
        |$I4$bitMapClass( $parentDimensionMask ), $bitMapClass( $parentAggregationMask ),
        |$I4$bitMapClass( $childDimensionMask ), $bitMapClass( $childAggregationMask )
        |$I3);
        |$I2$parentInstance = destinationCube;
        |$I3$releaseCubeMethod( $parentTmpInstance );
        |$I2}
        |$I2$releaseCubeMethod( $currentRelation );
        |$I2$currentInstance = null;
        |$I}""".stripMargin

  }

}
