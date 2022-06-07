/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.serializers.JavaSerializer
import org.burstsys.vitals.errors.VitalsException

import scala.util.{Failure, Success, Try}

/**
  * mix in behaviors for acting as the (possible failing) response in a request/response
  * message interchange
  *
  * @tparam R
  */
trait FabricNetRespMsg[R] extends AnyRef {

  /**
    *
    * @return
    */
  final
  def result: Try[R] = {
    if (_exception.nonEmpty) return Failure(_exception.get)
    if (_data.nonEmpty) return Success(_data.get)
    Failure(VitalsException(s"response was missing data and exception"))
  }

  private
  var _exception: Option[Throwable] = None

  private
  var _data: Option[R] = None

  final
  def isSuccess: Boolean = _exception.isEmpty

  final
  def isFailure: Boolean = _exception.nonEmpty

  final
  def success(data: R): this.type = {
    _data = Some(data)
    this
  }

  final
  def failure(t: Throwable): this.type = {
    _exception = Some(t)
    this
  }

  final
  def readResponse(kryo: Kryo, input: Input): Unit = {
    if (input.readBoolean)
      _data = Some(kryo.readClassAndObject(input).asInstanceOf[R])
    else
      _exception = Some(kryo.readObject(input, classOf[Throwable], new JavaSerializer()))
  }

  final
  def writeResponse(kryo: Kryo, output: Output): Unit = {
    output writeBoolean isSuccess
    if (isSuccess) {
      kryo.writeClassAndObject(output, _data.get)
    } else {
      kryo.writeObject(output, _exception.get, new JavaSerializer())
    }
  }


}
