/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.collectors.cube.FeltCubeCollector
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code._
import org.burstsys.vitals.strings.VitalsString

/**
 *
 */
trait FeltCubeSymbols {

  final val cubeIdValName = "cubeId"
  final val parentCubeIdValName = "parentCubeId"

  val cubeCollectorClassName: FeltCode = classOf[FeltCubeCollector]

  final private val feltCubeBinding = "feltBinding.collectors.cubes"

  final lazy val grabCubeMethod: FeltCode = s"$feltCubeBinding.grabCollector"

  final lazy val releaseCubeMethod: FeltCode = s"$feltCubeBinding.releaseCollector"

  /**
   * this is a pointer to a 'root' cube (presumably 1:1 with queries until we support cross root cube mutation)
   * There is one of these for each cube declared. This goes in the Runtime.
   *
   * @param frameName
   * @return
   */
  final
  def cubeRoot(frameName: String): FeltCode = s"cube_${frameName}_root"

  /**
   * TODO
   *
   * @param frameName
   * @return
   */
  final
  def cubeDictionary(frameName: String): FeltCode = s"cube_${frameName}_dictionary"

  final
  def cubeBuilderVariable(frameName: String): FeltCode = s"cube_${frameName}_builder"

  /**
   * TODO
   *
   * @param frameName
   * @param pathName
   * @return
   */
  final
  def cubeInstanceVariable(frameName: String, pathName: BrioPathName): FeltCode =
    s"cube_${frameName}_${pathName.replace('.', '_')}_instance"

  /**
   * TODO
   *
   * @param frameName
   * @param pathName
   * @return
   */
  def cubeRelationVariable(frameName: String, pathName: BrioPathName): FeltCode =
    s"cube_${frameName}_${pathName.replace('.', '_')}_relation"

  def dynamicRelationVar(visitTag: String): FeltCode = s"cube_dynamic_relation_$visitTag".camelCaseToUnderscore

  def rtDynamicRelation(visitTag: String): FeltCode = s"$sweepRuntimeSym.${dynamicRelationVar(visitTag)}"

  def staticInstanceSaveVar(visitTag: String): FeltCode = s"cube_static_instance_save_$visitTag".camelCaseToUnderscore

  def rtStaticInstanceSave(visitTag: String): FeltCode = s"$sweepRuntimeSym.${staticInstanceSaveVar(visitTag)}"


}
