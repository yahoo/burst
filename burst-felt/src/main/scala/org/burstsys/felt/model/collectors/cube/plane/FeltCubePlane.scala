/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.plane

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio
import org.burstsys.brio.dictionary.flex
import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.brio.dictionary.mutable.{BrioMutableDictionary, BrioMutableDictionaryAnyVal}
import org.burstsys.fabric.execution.model.gather.plane.FabricPlane
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeFactory
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorBuilder, FeltCollectorPlane, FeltCollectorPlaneContext}
import org.burstsys.vitals.errors.{VitalsException, safely}

/**
 * A [[FabricPlane]] for cube collectors
 */
trait FeltCubePlane extends FeltCollectorPlane[FeltCubeBuilder, FeltCubeCollector] {

  /**
   * narrow to specific type
   *
   * @return
   */
  def planeCollector: FeltCubeCollector

  /**
   * narrow to specific type
   *
   * @return
   */
  def planeBuilder: FeltCubeBuilder

  /**
   * mark dictionary overfloa happened
   */
  def flagDictionaryOverflow(): Unit

}

/**
 * Felt Version of Fabric Plane
 */
final
class FeltCubePlaneContext()
  extends FeltCollectorPlaneContext[FeltCubeBuilder, FeltCubeCollector]
    with FeltCubePlane with FeltCubePlaneMerge with FeltCubeFactory {

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _planeDictionary: BrioMutableDictionary = BrioMutableDictionaryAnyVal()

  private[this]
  var _dictionaryOverflow: Boolean = _

  private[this]
  var _rowLimitExceeded: Boolean = _

  private[this]
  var _rowCount: Int = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def planeDictionary: BrioMutableDictionary = _planeDictionary

  @inline override
  def planeDictionary_=(dictionary: BrioMutableDictionary): Unit = _planeDictionary = dictionary

  @inline override
  def dictionaryOverflow: Boolean = if (planeCollector == null) _dictionaryOverflow else _planeDictionary.overflowed

  @inline override
  def rowLimitExceeded: Boolean = if (planeCollector == null) _rowLimitExceeded else planeCollector.rowLimited

  @inline override
  def rowCount: Int = if (planeCollector == null) _rowCount else planeCollector.rowCount

  @inline override
  def clearCollector(): Unit = {
    super.clearCollector()
    _rowLimitExceeded = false
    _rowCount = 0
  }

  @inline override
  def clearDictionary(): Unit = {
    super.clearDictionary()
    _planeDictionary.reset()
    _dictionaryOverflow = false
  }

  @inline override
  def flagDictionaryOverflow(): Unit = {
    _dictionaryOverflow = true
    _planeDictionary.flagOverflow()
  }

  override def grabCollector(builder: FeltCubeBuilder): FeltCubeCollector =
    planeBinding.collectors.cubes.grabCollector(builder)

  override def releaseCollector(collector: FeltCubeCollector): Unit =
    planeBinding.collectors.cubes.releaseCollector(collector)

  override def newBuilder(): FeltCubeBuilder = planeBinding.collectors.cubes.newBuilder

  ////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ////////////////////////////////////////////////////////////////////////////////////////////

  override def init(builder: FeltCollectorBuilder): this.type = {
    super.init(builder)
    _planeDictionary = brio.dictionary.factory.grabMutableDictionary()
    this
  }

  @inline override
  def releaseResourcesOnMaster(): Unit = {
    super.releaseResourcesOnMaster()
    brio.dictionary.flex.releaseFlexDictionary(_planeDictionary.asInstanceOf[BrioFlexDictionary])
    _planeDictionary = BrioMutableDictionaryAnyVal()
  }

  @inline override
  def releaseResourcesOnWorker(): Unit = {
    super.releaseResourcesOnWorker()
    brio.dictionary.factory.releaseMutableDictionary(_planeDictionary)
    _planeDictionary = BrioMutableDictionaryAnyVal()
  }

  @inline override protected
  def resetConstraintFlags(): Unit = {
    super.resetConstraintFlags()
    _dictionaryOverflow = false
    _rowLimitExceeded = false
    _rowCount = 0
  }

  @inline override protected
  def transferTallies(): Unit = {
    super.transferTallies()
    _dictionaryOverflow = _planeDictionary.overflowed
    _rowLimitExceeded = planeCollector.rowLimited
    _rowCount = planeCollector.rowCount
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    lazy val tag = s"FeltCubePlane.write(planeId=${planeId}, planeName=${planeName})"
    try {
      super.write(kryo, output)

      output writeInt _rowCount
      output writeBoolean _rowLimitExceeded

      output writeBoolean _dictionaryOverflow
      output writeInt _planeDictionary.serializationSize
      _planeDictionary.write(kryo, output)

    } catch safely {
      case t: Throwable =>
        val msg = s"FELT_CUBE_PLANE_KRYO_WRITE_FAIL ${t.getMessage} $tag"
        throw VitalsException(msg, t)
    }
  }

  /**
   * reads are collecting results from worker slices for master aggregation.
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    lazy val tag = s"FeltCubePlane.read(planeId=${planeId}, planeName=${planeName})"
    try {
      super.read(kryo, input)

      _rowCount = input.readInt
      _rowLimitExceeded = input.readBoolean()

      _planeDictionary = null
      _dictionaryOverflow = input.readBoolean()
      val dictionarySize = input.readInt
      _planeDictionary = flex.grabFlexDictionary(startSize = dictionarySize)
      _planeDictionary.read(kryo, input)

    } catch safely {
      case t: Throwable =>
        if (_planeDictionary != null) flex.releaseFlexDictionary(_planeDictionary.asInstanceOf[BrioFlexDictionary])
        val msg = s"FELT_CUBE_PLANE_KRYO_READ_FAIL ${t.getMessage} $tag"
        throw VitalsException(msg, t)
    }
  }

}
