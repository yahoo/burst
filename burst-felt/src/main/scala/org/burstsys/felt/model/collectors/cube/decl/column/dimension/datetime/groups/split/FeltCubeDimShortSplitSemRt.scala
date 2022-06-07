/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioShortToPrimitive}
import org.burstsys.felt.model.tree.code._


/**
 *
 * @param s
 */
final case
class FeltCubeDimShortSplitSemRt(var s: Array[Short]) extends FeltCubeDimSplitSemRt {

  @inline def this() = this(null)

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimShortSplitSemRt].getName}(
        |${generateShortArrayCode(s)(cursor indentRight 1)}
        |$I)""".stripMargin

  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doShort(v: Short)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioShortToPrimitive(shortSplitSlice(s, v))
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
    s = kryo.readClassAndObject(in).asInstanceOf[Array[Short]]
  }

}
