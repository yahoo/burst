/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.execute.parameters

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.brioDataTypeFromClassTag
import org.burstsys.vitals.errors.VitalsException

import scala.reflect.ClassTag

/**
  * runtime instance of a signature
  */
trait FabricCall extends FabricSignature[FabricParameterValue] with KryoSerializable {

  /**
    * is a named parameter null? if it's missing it's null...
    *
    * @param name
    * @return
    */
  def nullParameter(name: String): Boolean

  /**
    * return a scalar val
    *
    * @param name
    * @tparam T
    * @return
    */
  def scalarParameter[T: ClassTag](name: String): T

}

object FabricCall {

  def apply(): FabricCall = new FabricCallContext(Array.empty)

  def apply(parameterSet: Array[FabricParameterValue]): FabricCall =
    new FabricCallContext(parameterSet: Array[FabricParameterValue])

  def apply(parameterSet: FabricParameterValue*): FabricCall =
    new FabricCallContext(parameterSet.toArray)

  def apply(parameterJson: String): FabricCall =
    new FabricCallContext(FabricParameterJson.jsonToValues(parameterJson))
}

final
class FabricCallContext(parameterSet: Array[FabricParameterValue])
  extends FabricSignatureContext[FabricParameterValue](parameterSet) with FabricCall with KryoSerializable {

  override def toString: String = {
    val pString = if (parameters == null) "" else parameters.mkString(", ")
    s"FabricCall($pString)"
  }

  def this() = this(Array.empty)

  override protected def allocateParameterArray(size: Int): Array[FabricParameterValue] = new Array[FabricParameterValue](size)

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def scalarParameter[T: ClassTag](name: String): T = {
    val vType = brioDataTypeFromClassTag[T]
    parameters.find(_.name == name) match {
      case None => throw VitalsException(s"parameter $name not found")
      case Some(p) => vType match {
        case BrioTypes.BrioBooleanKey => p.asScalar[Boolean].asInstanceOf[T]
        case BrioTypes.BrioByteKey => p.asScalar[Byte].asInstanceOf[T]
        case BrioTypes.BrioShortKey => p.asScalar[Short].asInstanceOf[T]
        case BrioTypes.BrioIntegerKey => p.asScalar[Int].asInstanceOf[T]
        case BrioTypes.BrioLongKey => p.asScalar[Long].asInstanceOf[T]
        case BrioTypes.BrioDoubleKey => p.asScalar[Double].asInstanceOf[T]
        case BrioTypes.BrioStringKey => p.asScalar[String].asInstanceOf[T]
        case _ => ???
      }
    }
  }

  override def nullParameter(name: String): Boolean = parameters.find(_.name == name) match {
    case None => false
    case Some(p) => p.isNull
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    // nothing else?
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    // nothing else?
  }

}
