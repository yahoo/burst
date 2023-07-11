/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.runtime

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.execution.model.gather.plane.FabricPlane
import org.burstsys.felt
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.frame.FeltFrameElement
import org.burstsys.tesla.part.teslaBuilderUseDefaultSize
import org.burstsys.vitals.errors.{VitalsException, _}

/**
 * A [[FabricPlane]] that uses cubes to hold results
 */
abstract
class FeltCollectorPlane[B <: FeltCollectorBuilder, C <: FeltCollector]
  extends FabricPlane with FeltFrameElement {

  /**
   *
   * @return
   */
  def planeCollector: FeltCollector

  /**
   *
   * @param c
   */
  def planeCollector_=(c: FeltCollector): Unit

  def planeBinding: FeltBinding

  /**
   *
   * @return
   */
  def planeBuilder: B

  def newBuilder(): B

  /**
   *
   * @param uid
   * @param planeName
   * @param builder
   * @param binding
   * @return
   */
  def init(builder: FeltCollectorBuilder): this.type
}

/**
 * Felt Version of Fabric Plane
 */
abstract
class FeltCollectorPlaneContext[B <: FeltCollectorBuilder, C <: FeltCollector]
  extends FeltCollectorPlane[B, C] with FeltCollectorFactory[B, C] {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  var _planeId: Int = -1

  @transient private[this]
  var _planeName: String = "namelessPlane"

  @transient private[this]
  var _planeBuilder: B = _

  @transient private[this]
  var _planeCollector: C = _

  @transient private[this]
  var _planeBinding: FeltBinding = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // FabricPlane
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def frameId: Int = planeId

  @inline final override
  def frameName: String = planeName

  @inline final override
  def planeId: Int = _planeId

  @inline final override
  def planeName: String = _planeName

  @inline final override
  def planeCollector: C = _planeCollector

  @inline final override
  def planeCollector_=(c: FeltCollector): Unit = _planeCollector = c.asInstanceOf[C]

  @inline final override
  def planeBuilder: B = _planeBuilder

  @inline final override
  def planeBinding: FeltBinding = _planeBinding

  @inline override
  def clear(): Unit = {
    clearCollector()
    clearDictionary()
  }

  @inline override
  def clearCollector(): Unit = {
    _planeCollector.clear()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Called the first time the gather is allocated from a parts shop.
   *
   * @return
   */
  @inline override
  def init(builder: FeltCollectorBuilder): this.type = {
    resetConstraintFlags()
    initializeOutcome()
    _planeId = builder.frameId
    _planeName = builder.frameName
    _planeBinding = builder.binding
    _planeBuilder = builder.asInstanceOf[B]
    _planeCollector = grabCollector(_planeBuilder, 0)
    resetConstraintFlags()
    this
  }

  @inline override
  def releaseResourcesOnWorker(): Unit = {
    /// save constraint flags for use after we release the cube and dictionary
    transferTallies()

    // now we can release the collector and dictionary
    releaseCollector(_planeCollector)
    _planeCollector = null.asInstanceOf[C]
  }


  @inline override
  def releaseResourcesOnSupervisor(): Unit = {
    /// save constraint flags for use after we release the cube and dictionary
    transferTallies()

    // now we can release the collector and dictionary
    releaseCollector(_planeCollector)
    _planeCollector = null.asInstanceOf[C]
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    lazy val tag = s"FeltCollectorPlane.write(planeId=${planeId}, planeName=${planeName})"
    try {

      transferTallies()

      output writeInt _planeId
      output writeString _planeName

      output writeString _planeBinding.name

      writeOutcome(kryo, output)

      _planeBuilder.write(kryo, output)
      output writeInt _planeCollector.size()
      _planeCollector.write(kryo, output)
    } catch safely {
      case t: Throwable =>
        val msg = s"FELT_COLLECTOR_PLANE_KRYO_WRITE_FAIL ${t.getMessage} $tag"
        throw VitalsException(msg, t)
    }
  }

  /**
   * reads are collecting results from worker slices for supervisor aggregation.
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    lazy val tag = s"FeltCollectorPlane.read(planeId=${planeId}, planeName=${planeName})"
    try {

      _planeId = input.readInt()
      _planeName = input.readString

      _planeBinding = felt.binding.bindingLookup(input.readString)

      readOutcome(kryo, input)

      _planeBuilder = newBuilder()
      _planeBuilder.read(kryo, input)

      val desiredSize = input.readInt()
      _planeCollector = grabCollector(_planeBuilder, desiredSize)
      _planeCollector.read(kryo, input)
    } catch safely {
      case t: Throwable =>
        val msg = s"FELT_COLLECTOR_PLANE_KRYO_READ_FAIL ${t.getMessage} $tag"
        throw VitalsException(msg, t)
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline protected
  def resetConstraintFlags(): Unit = {
  }

  @inline protected
  def transferTallies(): Unit = {
  }

  override
  def toString: String = {
    s"""FeltCollectorPlane(
       | planeId=${planeId}, planeName=${planeName} rowCount=${rowCount} rowLimitExceeded=${rowLimitExceeded}
       |  $outcomeAsString
       |)
     """.stripMargin
  }

}
