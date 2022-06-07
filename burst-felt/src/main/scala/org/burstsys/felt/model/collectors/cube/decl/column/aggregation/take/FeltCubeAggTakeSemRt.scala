/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.tree.source.S

import scala.language.postfixOps

/**
 * A take semantic is an aggregation that controls taking a subset of the rowset based on the [[FeltCubeTakeSemMode]]:
 * These are all 'pseudo' operations because only a full sort across all
 * rows across all partitions on all worker nodes would provide that - and that is prohibitively expensive for
 * little real value in our applications.
 *
 * @param _mode     the mode for this take
 * @param _scatterK the sort/truncate/randomize set size for the operation on the master
 * @param _sliceK   the sort/truncate/randomize set size for the operation on on each worker node partition.
 *                  If set to -1, then this defaults to 10 * finalK.
 * @param _itemK    the sort/truncate/randomize set size foreach blob in blob region. If set to -1, then
 *                  this defaults to 10 * partitionK.
 */
final case
class FeltCubeAggTakeSemRt(var _mode: FeltCubeTakeSemMode, var _scatterK: Int, var _sliceK: Int, var _itemK: Int)
  extends AnyRef with FeltCubeAggSemRt {

  ///////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ///////////////////////////////////////////////////////////////////////////////////

  def scatterK: Int = _scatterK

  def sliceK: Int = if (_sliceK != -1) _sliceK else _scatterK * 2

  def regionK: Int = if (_itemK != -1) _itemK else _scatterK * 2

  ///////////////////////////////////////////////////////////////////////////////////
  // code generation
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"""|
        |$I ${classOf[FeltCubeAggTakeSemRt].getName}(
        |${_mode.generateCode(cursor indentRight)},
        |$I  ${_scatterK}, ${_sliceK}, ${_itemK}
        |$I )""".stripMargin

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime operations
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioBooleanToPrimitive(brioPrimitiveToBoolean(a) || brioPrimitiveToBoolean(b))

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioDoubleToPrimitive(brioPrimitiveToDouble(a) + brioPrimitiveToDouble(b))

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = ???

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeClassAndObject(output, _mode)
    output writeInt _scatterK
    output writeInt _sliceK
    output writeInt _itemK
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    _mode = kryo.readClassAndObject(input).asInstanceOf[FeltCubeTakeSemMode]
    _scatterK = input.readInt
    _sliceK = input.readInt
    _itemK = input.readInt
  }


  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"""$S${classOf[FeltCubeAggTakeSemRt].getName}(
       |${_mode.normalizedSource(index + 1)},
       |$S  ${_scatterK}, ${_sliceK}, ${_itemK}
       |$S)""".stripMargin
}
