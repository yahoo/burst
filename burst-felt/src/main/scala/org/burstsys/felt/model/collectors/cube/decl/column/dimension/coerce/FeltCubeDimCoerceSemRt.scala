/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.coerce

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.ginsu.functions.GinsuFunctions
import org.burstsys.vitals.errors._

/**
 * Takes what it is given in the call and attempts to convert it to the type of this Coerce
 *
 * @param bType
 */
final case
class FeltCubeDimCoerceSemRt(var bType: BrioTypeKey) extends AnyRef
  with FeltCubeDimSemRt with GinsuFunctions {

  semanticType = COERCE_DIMENSION_SEMANTIC

  override val dimensionHandlesStrings: Boolean = true

  @inline override
  def doString(d: BrioDictionary, v: String)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    try {
      bType match {
        case BrioBooleanKey => brioBooleanToPrimitive(v.toBoolean)
        case BrioByteKey => brioByteToPrimitive(v.toByte)
        case BrioShortKey => brioShortToPrimitive(v.toShort)
        case BrioIntegerKey => brioIntegerToPrimitive(v.toInt)
        case BrioLongKey => brioLongToPrimitive(v.toLong)
        case BrioDoubleKey => brioDoubleToPrimitive(v.toDouble)
        case BrioStringKey => brioStringToPrimitive(d, v)(threadRuntime.text)
        case _ => throw new RuntimeException(s"unknown value type $bType")
      }
    } catch safely {
      case e: NumberFormatException => 0L
    }
  }

  @inline override
  def doLexiconString(srcDictionary: BrioDictionary, destDictionary: BrioDictionary, srcKey: BrioDictionaryKey)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = {
    ??? // TODO LEXICON
    try {
      val dString = destDictionary.stringLookup(srcKey)(threadRuntime.text)
      bType match {
        case BrioBooleanKey => brioBooleanToPrimitive(dString.toBoolean)
        case BrioByteKey => brioByteToPrimitive(dString.toByte)
        case BrioShortKey => brioShortToPrimitive(dString.toShort)
        case BrioIntegerKey => brioIntegerToPrimitive(dString.toInt)
        case BrioLongKey => brioLongToPrimitive(dString.toLong)
        case BrioDoubleKey => brioDoubleToPrimitive(dString.toDouble)
        case BrioStringKey => brioStringToPrimitive(destDictionary, dString)(threadRuntime.text)
        case _ => throw new RuntimeException(s"unknown value type $bType")
      }
    } catch safely {
      case e: NumberFormatException => 0L
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"${classOf[FeltCubeDimCoerceSemRt].getName}($bType)"

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def write(kryo: Kryo, out: Output): Unit = {
    super.write(kryo, out)
    out writeByte bType
  }

  @inline override
  def read(kryo: Kryo, in: Input): Unit = bType = {
    super.read(kryo, in)
    in.readByte
  }


  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"${classOf[FeltCubeDimCoerceSemRt].getName}($bType)"
}
