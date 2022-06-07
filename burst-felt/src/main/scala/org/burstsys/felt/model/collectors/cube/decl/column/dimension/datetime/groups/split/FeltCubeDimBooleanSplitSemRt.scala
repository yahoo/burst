/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioBooleanToPrimitive}
import org.burstsys.felt.model.tree.code._


/**
 *
 * @param s
 */
final case
class FeltCubeDimBooleanSplitSemRt(var s: Array[Boolean]) extends FeltCubeDimSplitSemRt {

  @inline def this() = this(null)

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|$I${classOf[FeltCubeDimBooleanSplitSemRt].getName}(
        |${generateBooleanArrayCode(s)(cursor indentRight 1)}
        |$I)""".stripMargin


  @inline override
  def doBoolean(v: Boolean)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioBooleanToPrimitive(booleanSplitSlice(s, v))
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def write(kryo: Kryo, out: Output): Unit = {
    super.write(kryo, out)
    kryo.writeClassAndObject(out, s)
  }

  @inline override
  def read(kryo: Kryo, in: Input): Unit = {
    super.read(kryo, in)
    s = kryo.readClassAndObject(in).asInstanceOf[Array[Boolean]]
  }

}
