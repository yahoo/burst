/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext}

trait ZapCubeDimensioner extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * used to pass the cursor state from a parent cube to a child cube (this one that is)
   *
   * @param parentCube
   * @return
   */
  @inline final override
  def inheritCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, parentCube: FeltCubeCollector): FeltCubeCollector = {
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    val pc = parentCube.asInstanceOf[ZapCubeContext]
    var i = 0
    while (i < tc.cursorKeyLength) {
      tc.cursorKey.data(i) = pc.cursorKey.data(i)
      i += 1
    }
    this
  }

  @inline final override
  def initCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = {
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.cursorUpdated = true
    tc.cursorKey.clear
    tc.cursorRow = cube.ZapCubeRow()
  }

  @inline final override
  def writeDimension(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = {
    navigate(builder.asInstanceOf[ZapCubeBuilder], thisCube.asInstanceOf[ZapCubeContext])
  }

  @inline final override
  def writeDimensionNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int): Unit = {
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.cursorKey.writeKeyDimensionNull(dimension)
    tc.cursorUpdated = true
  }

  @inline final override
  def writeDimensionPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int, value: BrioPrimitive): Unit = {
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.cursorKey.writeKeyDimensionPrimitive(dimension, value)
    tc.cursorUpdated = true
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Navigate if the cursor has been updated since the last dimension update
   *
   * @return
   */
  @inline final
  def navigate(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapCubeContext = {
    if (thisCube.cursorUpdated) {
      thisCube.cursorRow = thisCube.navigate(builder, thisCube, thisCube.cursorKey)
      thisCube.cursorUpdated = false
    }
    thisCube
  }

}
