/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioDoubleToPrimitive}
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.source._


final case
class FeltCubeDimDoubleSplitSemRt(var s: Array[Double]) extends FeltCubeDimSplitSemRt {

  @inline def this() = this(null)

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimDoubleSplitSemRt].getName}(
        |${generateDoubleArrayCode(s)(cursor indentRight 1)}
        |$I)""".stripMargin


  @inline override
  def doDouble(v: Double)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioDoubleToPrimitive(doubleSplitSlice(s, v))
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
    s = kryo.readClassAndObject(in).asInstanceOf[Array[Double]]
  }


  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////
  def normalizedSource(implicit index: Int): String =
    s"""$S${classOf[FeltCubeDimDoubleSplitSemRt].getName}(
       |${generateDoubleArray(s)(index + 1)}
       |$S)""".stripMargin
}
