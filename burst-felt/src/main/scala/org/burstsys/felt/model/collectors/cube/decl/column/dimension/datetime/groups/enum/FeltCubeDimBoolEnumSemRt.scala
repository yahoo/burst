/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioBooleanToPrimitive}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, generateBooleanArrayCode}
import org.burstsys.ginsu.functions.GinsuFunctions


/**
 *
 * @param values
 */
final case
class FeltCubeDimBoolEnumSemRt(var values: Array[Boolean]) extends FeltCubeDimEnumSemRt with GinsuFunctions {

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimBoolEnumSemRt].getName}(
        |${generateBooleanArrayCode(values)(cursor indentRight 1)}
        |$I)""".stripMargin


  @inline override
  def doBoolean(v: Boolean)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioBooleanToPrimitive(booleanEnumSlice(values, v))
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def write(kryo: Kryo, out: Output): Unit = {
    super.write(kryo, out)
    kryo.writeClassAndObject(out, values)
  }

  @inline override
  def read(kryo: Kryo, in: Input): Unit = {
    super.read(kryo, in)
    values = kryo.readClassAndObject(in).asInstanceOf[Array[Boolean]]
  }

}
