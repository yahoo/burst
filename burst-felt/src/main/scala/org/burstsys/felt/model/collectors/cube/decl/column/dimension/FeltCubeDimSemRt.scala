/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeSemRt
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, cleanClassName}

/**
 * cube semantics runtime support for dimensions
 */
trait FeltCubeDimSemRt extends AnyRef with FeltCubeSemRt {

  //////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  //////////////////////////////////////////////////////////////////////

  private[this]
  var _semanticType: FeltDimSemType = _

  //////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////

  final def semanticType: FeltDimSemType = _semanticType

  final def semanticType_=(t: FeltDimSemType): Unit = _semanticType = t

  /**
   * most dimensions don't handle 'strings' with meaningful or useful semantics.
   * this flag is used to check they are not applied my mistake
   *
   * @return
   */
  protected def dimensionHandlesStrings: Boolean

  /**
   * generate this semantic as a scala declaration
   *
   * @param cursor
   * @return
   */
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${cleanClassName(getClass)}()"

  /**
   * process a Boolean value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doBoolean(a: Boolean)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a Byte value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doByte(a: Byte)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a Short value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doShort(a: Short)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process an Int value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doInteger(a: Int)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a Long value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doLong(a: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a Double value
   *
   * @param a
   * @param threadRuntime
   * @return
   */
  def doDouble(a: Double)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a String value where the lexicon is '''not''' used
   *
   * @param a
   * @param threadRuntime
   * @return
   * @deprecated this will be conflated with `doLexiconString()` when the lexicon is fully vetted
   */
  def doString(d: BrioDictionary, a: String)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  /**
   * process a String value where the lexicon '''is''' used. Note that since we are doing copying from
   * one dictionary to another e.g. lattice brio blob dictionary to cube dictionary, we need to get the
   * string from the srcDictionary via the sourceKey and then  to dest and then return the key from the dest
   *
   * @param srcDictionary  the source dictionary
   * @param srcKey         the dictionary key in the source dictionary
   * @param destDictionary the destination dictionary
   * @return the string key in the destination dictionary
   * @param threadRuntime
   * @deprecated this will be conflated with `doString()` when the lexicon is fully vetted
   */
  def doLexiconString(srcDictionary: BrioDictionary, destDictionary: BrioDictionary, srcKey: BrioDictionaryKey)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = ???

  //////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  //////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, _semanticType)
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    _semanticType = kryo.readClassAndObject(input).asInstanceOf[FeltDimSemType]
  }

}
