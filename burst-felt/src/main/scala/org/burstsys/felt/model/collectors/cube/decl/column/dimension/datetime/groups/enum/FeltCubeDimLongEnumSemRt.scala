/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioLongToPrimitive}
import org.burstsys.felt.model.tree.code._
import org.burstsys.ginsu.functions.GinsuFunctions


/**
 *
 * @param values
 */
final case
class FeltCubeDimLongEnumSemRt(var values: Array[Long]) extends FeltCubeDimEnumSemRt with GinsuFunctions {

  @inline def this() = this(null)

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimLongEnumSemRt].getName}(
        |${generateLongArrayCode(values)(cursor indentRight 1)}
        |$I)""".stripMargin


  @inline override
  def doLong(v: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioLongToPrimitive(longEnumSlice(values, v))
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
    values = kryo.readClassAndObject(in).asInstanceOf[Array[Long]]
  }

}
