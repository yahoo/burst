/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, brioStringToPrimitive}
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.source._
import org.burstsys.ginsu.functions.GinsuFunctions


/**
 *
 * @param values
 */
final case
class FeltCubeDimStrEnumSemRt(var values: Array[String]) extends FeltCubeDimEnumSemRt with GinsuFunctions {

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I${classOf[FeltCubeDimStrEnumSemRt].getName}(
        |${generateStringArrayCode(values)(cursor indentRight 1)}
        |$I)""".stripMargin

  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doString(d: BrioDictionary, v: String)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    brioStringToPrimitive(d, stringEnumSlice(values, v))(threadRuntime.text)
  }

  @inline override
  def doLexiconString(srcDictionary: BrioDictionary, destDictionary: BrioDictionary, srcKey: BrioDictionaryKey)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    val str = srcDictionary.stringLookup(srcKey)(threadRuntime.text)
    brioStringToPrimitive(destDictionary, stringEnumSlice(values, str))(threadRuntime.text)
    // TODO  can this be done with zero or one dictionary lookup?
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
    values = kryo.readClassAndObject(in).asInstanceOf[Array[String]]
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////
  def normalizedSource(implicit index: Int): String =
    s"""$S${classOf[FeltCubeDimStrEnumSemRt].getName}(
       |${generateStringArray(values)(index + 1)}
       |$S)""".stripMargin

}
