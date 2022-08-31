/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.runtime

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.felt
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.frame.FeltFrameElement
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.vitals.errors.{VitalsException, safely}

/**
 *
 */
trait FeltCollectorBuilder extends AnyRef
  with TeslaPartBuilder with KryoSerializable with FeltFrameElement {

  /**
   * local bindings
   *
   * @return
   */
  def binding: FeltBinding

  /**
   * construct a new collector plane of a specific type
   *
   * @param frameId
   * @param frameName
   * @return
   */
  def newCollectorPlaneOnWorker(): FeltCollectorPlane[_, _]

  /**
   * the collector plane class appropriate for this builder
   *
   * @tparam C
   * @return
   */
  def collectorPlaneClass[C <: FeltCollectorPlane[_, _]]: Class[C]

  def requiredMemorySize: TeslaMemorySize

  /**
   * basic initialization of a builder
   *
   * @param frameId
   * @param frameName
   * @return
   */
  def init(frameId: Int, frameName: String, binding: FeltBinding): this.type

}

/**
 * generic collector Builder super type
 */
abstract
class FeltCollectorBuilderContext extends AnyRef with FeltCollectorBuilder {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _frameId: Int = -1

  private[this]
  var _frameName: String = "NO_NAME"

  private[this]
  var _binding: FeltBinding = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def frameId: Int = _frameId

  final override
  def frameName: String = _frameName

  final override
  def binding: FeltBinding = _binding

  final
  def newCollectorPlaneOnWorker(): FeltCollectorPlane[_, _] = {
    try {
      collectorPlaneClass.getDeclaredConstructor().newInstance().asInstanceOf[FeltCollectorPlane[_, _]].init(builder = this)
    } catch safely {
      case t: Throwable =>
        throw t
    }
  }

  final override
  def init(frameId: Int, frameName: String, binding: FeltBinding): this.type = {
    _frameId = frameId
    _frameName = frameName
    _binding = binding
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output) {
    try {
      output writeInt _frameId
      output writeString _frameName
      output writeString _binding.name
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def read(kryo: Kryo, input: Input) {
    try {
      _frameId = input.readInt
      _frameName = input.readString
      _binding = felt.binding.bindingLookup(input.readString)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
