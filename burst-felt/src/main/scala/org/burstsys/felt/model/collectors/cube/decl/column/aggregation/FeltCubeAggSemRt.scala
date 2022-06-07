/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioPrimitives
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeSemRt
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}


/**
 * cube semantics runtime support for aggregations
 * Cubes need to know how to do the basic aggregation semantics inherently since they are performed
 * during merges and joins. These are the basic operations supported. These are KRYO serialized and
 * normal Scala Serialized (the latter for JSON)
 * if intra is true, then this is a merge within an Item traversal. If it is false
 * then it refers to a merge that is happening across items.
 *
 */
trait FeltCubeAggSemRt extends AnyRef with FeltCubeSemRt {

  //////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  //////////////////////////////////////////////////////////////////////

  private[this]
  var _semanticType: FeltAggSemType = _

  //////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////

  final def semanticType: FeltAggSemType = _semanticType

  final def semanticType_=(t: FeltAggSemType): Unit = _semanticType = t

  /**
   * generate this aggregation as a scala ''declaration''
   *
   * @param cursor
   * @return
   */
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doBooleanInit(): BrioPrimitive = BrioPrimitives.brioBooleanToPrimitive(false)

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doByteInit(): BrioPrimitive = 0L

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doShortInit(): BrioPrimitive = 0L

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doIntegerInit(): BrioPrimitive = 0L

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doLongInit(): BrioPrimitive = 0L

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  /**
   * its important to initialize numbers to non ''default'' values sometimes
   *
   * @return
   */
  def doDoubleInit(): BrioPrimitive = BrioPrimitives.brioDoubleToPrimitive(0.0)

  /**
   * process a value
   *
   * @param a
   * @param b
   * @param intra
   * @return
   */
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive

  //////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  //////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, _semanticType)
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    _semanticType = kryo.readClassAndObject(input).asInstanceOf[FeltAggSemType]
  }


}
