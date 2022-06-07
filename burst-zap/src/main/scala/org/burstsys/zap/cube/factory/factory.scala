/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.factory.TeslaPartFactory
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

import scala.collection.mutable

/**
 * == factory for cubes ==
 * right now we have size tiered allocation for cubes as well as blocks underneath. The reason for
 * this (which may turn out to be wrong) is that we may want to upon sensing memory limits being
 * reached, start to deallocate the larger sizes first. We shall see...
 */
package object factory extends TeslaPartFactory[ZapCubeContext, ZapCubePool] with ZapCubeShop {

  val cubeFactoryPath: String = this.getClass.getName.stripSuffix(".package$")

  def shop: ZapCubeShop = this

  val trackAllocations = false
  val allocated = new mutable.HashSet[Long]

  startPartTender

  @inline final override
  def grabZapCube(builder: ZapCubeBuilder): ZapCubeContext = {
    val bs = TeslaBlockSizes findBlockSize builder.totalMemorySize
    val cube = perThreadPartPool(bs) grabZapCube (builder /*, dictionary*/)
    if (trackAllocations)
      allocated synchronized {
        if (allocated.contains(cube.cubeDataStart))
          throw VitalsException(s"ZapCube ${cube.cubeDataStart} already allocated")
        allocated += cube.cubeDataStart
      }
    cube.validateAndIncrementReferenceCount()
    cube
  }


  @inline final override
  def releaseZapCube(cube: ZapCubeContext): Unit = {
    cube.validateAndDecrementReferenceCount()
    poolByPoolId(cube.poolId).releaseZapCube(cube)
    if (trackAllocations) {
      allocated synchronized {
        if (!allocated.contains(cube.cubeDataStart))
          throw VitalsException(s"ZapCube ${cube.cubeDataStart} not allocated")
        allocated -= cube.cubeDataStart
      }
    }
  }

  @inline final override
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): ZapCubePool =
    ZapCubePool(poolId, size)

}
