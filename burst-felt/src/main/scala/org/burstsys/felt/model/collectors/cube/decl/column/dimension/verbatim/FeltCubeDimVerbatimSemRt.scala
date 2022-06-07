/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.ginsu.functions.coerce.GinsuCoerceFunctions

/**
 * Convert a string into a specific Brio Data Type
 *
 * @param bType
 */
final case
class FeltCubeDimVerbatimSemRt(var bType: BrioTypeKey = 0.toByte) extends AnyRef
  with FeltCubeDimSemRt with GinsuCoerceFunctions {

  semanticType = VERBATIM_DIMENSION_SEMANTIC

  override protected val dimensionHandlesStrings: Boolean = true

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeDimVerbatimSemRt].getName}($bType)"

  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(v: Boolean)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioBooleanToPrimitive(v)

  @inline override
  def doByte(v: Byte)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioByteToPrimitive(v)

  @inline override
  def doShort(v: Short)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioShortToPrimitive(v)

  @inline override
  def doInteger(v: Int)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioIntegerToPrimitive(v)

  @inline override
  def doLong(v: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioLongToPrimitive(v)

  @inline override
  def doDouble(v: Double)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = brioDoubleToPrimitive(v)

  @inline override
  def doString(d: BrioDictionary, v: String)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
    brioStringToPrimitive(d, v)(threadRuntime.text)

  @inline override
  def doLexiconString(srcDictionary: BrioDictionary, destDictionary: BrioDictionary, srcKey: BrioDictionaryKey)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    // TODO LEXICON
    val srcString = srcDictionary.stringLookup(srcKey)(threadRuntime.text)
    val destKey = destDictionary.keyLookupWithAdd(srcString)(threadRuntime.text)
    brioShortToPrimitive(destKey)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def write(kryo: Kryo, out: Output): Unit = {
    super.write(kryo, out)
    out writeByte bType
  }

  @inline override
  def read(kryo: Kryo, in: Input): Unit = {
    super.read(kryo, in)
    bType = in.readByte
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////
  def normalizedSource(implicit index: Int): String =
    s"$S${classOf[FeltCubeDimVerbatimSemRt].getName}($bType)"
}
