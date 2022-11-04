/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.control

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.serializers.JavaSerializer
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.execution.model.scanner.FabricScanner
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

/**
 * A gather that wraps a fault that happened on the worker
 * TODO this needs to be modified to use super class exception management
 */
trait FabricFaultGather extends FabricControlGather {

  /**
   * the fault as a throwable
   *
   * @return
   */
  def fault: Throwable

}

object FabricFaultGather {

  def apply(scanner: FabricScanner, fault: Throwable): FabricFaultGather =
    FabricFaultGatherContext().initialize(scanner).recordFault(fault: Throwable)

}

private[fabric] final case
class FabricFaultGatherContext() extends FabricControlGatherContext
  with FabricFaultGather with KryoSerializable {

  def recordFault(fault: Throwable): FabricFaultGather = {
    _fault = fault
    markException(_fault)
    this
  }

  /////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _fault: Throwable = _

  /////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////

  override def fault: Throwable = _fault

  override def resultMessage: String =
    s"""|FAULT(groupKey=$groupKey)
        |----------------------------------------
        |${_fault}
        |----------------------------------------""".stripMargin

  /////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  /////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      super.write(kryo, output)
      val cleanException = {
        // make sure we don't try to serialize anything but a basic runtime exception
        val e = new RuntimeException(_fault.getMessage)
        e.setStackTrace(_fault.getStackTrace)
        e
      }
      kryo.writeObject(output, cleanException, new JavaSerializer())
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAB_FAULT_GATHER_WRITE_FAIL $t", t)
        throw t
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      super.read(kryo, input)
      _fault = kryo.readObject(input, classOf[Throwable], new JavaSerializer())
      markException(_fault)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAB_FAULT_GATHER_READ_FAIL $t", t)
        throw t
    }
  }

}
