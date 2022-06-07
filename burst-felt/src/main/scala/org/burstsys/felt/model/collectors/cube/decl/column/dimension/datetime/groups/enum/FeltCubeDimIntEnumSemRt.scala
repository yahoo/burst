/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioIntegerToPrimitive}
import org.burstsys.felt.model.tree.code._
import org.burstsys.ginsu.functions.GinsuFunctions


/**
 *
 * @param values
 */
final case
class FeltCubeDimIntEnumSemRt(var values: Array[Int]) extends FeltCubeDimEnumSemRt with GinsuFunctions {

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimIntEnumSemRt].getName}(
        |${generateIntArrayCode(values)(cursor indentRight 1)}
        |$I)""".stripMargin

  @inline override
  def doInteger(v: Int)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioIntegerToPrimitive(intEnumSlice(values, v))
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
    values = kryo.readClassAndObject(in).asInstanceOf[Array[Int]]
  }

}
