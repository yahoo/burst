/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal

package object calculus {

  /**
   * cube metadata necessary for the [[FeltCubeCalculus]] to set up merges and joins
   *
   * @param pathKey
   * @param pathName
   * @param dimensions
   * @param dimensionMask
   * @param aggregations
   * @param aggregationMask
   */
  final case
  class Cube(
              pathKey: BrioPathKey, pathName: BrioPathName,
              dimensions: Array[Dim], dimensionMask: VitalsBitMapAnyVal,
              aggregations: Array[Agg], aggregationMask: VitalsBitMapAnyVal
            ) {
    override def toString: String =
      s"cube(path=$pathName:$pathKey, \t${
        dimensions.map(_.toString).mkString("\n\t\t", ", ", "")
      }, dimMask=${dimensionMask.asString}, ${
        aggregations.map(_.toString).mkString("\n\t\t", ", ", "")
      }, aggMask=${aggregationMask.asString} \n\t)"
  }

  /**
   * dimension metadata necessary for the [[FeltCubeCalculus]] to set up merges and joins
   *
   * @param name
   * @param index
   */
  final case
  class Dim(name: String, index: Int) {
    override def toString: String = s"dim($name:$index)"
  }

  /**
   * aggregation metadata necessary for the [[FeltCubeCalculus]] to set up merges and joins
   *
   * @param name
   * @param index
   */
  final case
  class Agg(name: String, index: Int) {
    override def toString: String = s"agg($name:$index)"
  }

  /**
   * all the metadata necessary to join a child cube into a parent cube
   *
   * @param childCube
   * @param parentCube
   */
  final case
  class Join(childCube: Cube, parentCube: Cube) {
    override def toString: BrioPathName =
      s"Join(${
        childCube.pathName
      }:${
        childCube.dimensionMask.asString
      }:${
        childCube.aggregationMask.asString
      }, ${
        parentCube.pathName
      }:${
        parentCube.dimensionMask.asString
      }:${
        parentCube.aggregationMask.asString
      })"
  }

  /**
   * all the metadata necessary to code generate for cube merges
   *
   * @param cube
   */
  final case
  class Merge(cube: Cube) {
    override def toString: BrioPathName =
      s"Merge(${cube.pathName}:${cube.dimensionMask.asString}:${cube.aggregationMask.asString})"
  }

}
