/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result

import java.io.{PrintWriter, StringWriter}
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.status.{FabricResultStatus, FabricSuccessResultStatus}
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

/**
 * Outer object to send back with a result that contains a result status and an
 * optional result group
 */
trait FabricExecuteResult extends VitalsJsonRepresentable[FabricExecuteResult] with FabricResult {

  def resultGroup: Option[FabricResultGroup]

  final override
  def toString: String = s"resultStatus=$resultStatus, resultMessage=$resultMessage"

}

object FabricExecuteResult {

  def apply(status: FabricResultStatus, message: String, t: Throwable): FabricExecuteResult = {
    val writer = new StringWriter
    writer.write(s"$message\n")
    val printer = new PrintWriter(writer)
    t.printStackTrace(printer)
    FabricExecuteResultContext(status, writer.toString, resultGroup = None)
  }

  def apply(
             resultStatus: FabricResultStatus = FabricSuccessResultStatus,
             resultMessage: String = "ok",
             resultGroup: Option[FabricResultGroup] = None
           ): FabricExecuteResult =
    FabricExecuteResultContext(
      resultStatus = resultStatus,
      resultMessage = resultMessage,
      resultGroup = resultGroup
    )

  def apply(resultGroup: FabricResultGroup): FabricExecuteResult =
    FabricExecuteResultContext(
      resultStatus = resultGroup.resultStatus,
      resultMessage = resultGroup.resultMessage,
      resultGroup = Some(resultGroup)
    )
}

final case
class FabricExecuteResultContext(
                                  var resultStatus: FabricResultStatus,
                                  var resultMessage: String,
                                  var resultGroup: Option[FabricResultGroup]
                                ) extends FabricExecuteResult with KryoSerializable with VitalsJsonObject {


  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricExecuteResult = FabricExecuteResultContext(resultStatus, resultMessage, resultGroup.map(_.toJson))

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      resultStatus = kryo.readClassAndObject(input).asInstanceOf[FabricResultStatus]
      resultMessage = input.readString
      resultGroup = kryo.readClassAndObject(input).asInstanceOf[Option[FabricResultGroup]]
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, resultStatus)
      output writeString resultMessage
      kryo.writeClassAndObject(output, resultGroup)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
