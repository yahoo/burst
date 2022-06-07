/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.runtime

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal

import scala.collection.mutable.ArrayBuffer
import scala.language.{implicitConversions, postfixOps}


/**
 * A specialized kryo serializable helper class to support fast lookup of whether a column (dimension or aggregation)
 * is 'active' at a given cube level - a '1' means the column is active for that cube, '0' if it is invisible.
 */
object FeltCubeTreeMask {

  ///////////////////////////////////////////////////////////
  // type conversions
  ///////////////////////////////////////////////////////////

  implicit def longArrayToCubeView(data: Array[Long]): FeltCubeTreeMask = FeltCubeTreeMask(data)

  implicit def cubeViewToLongArray(view: FeltCubeTreeMask): Array[Long] = view.data

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE (DECODING)
  ///////////////////////////////////////////////////////////////////////////////////
  def apply(kryo: Kryo, in: Input): FeltCubeTreeMask = {
    val length = in.readByte
    val a = new Array[Long](length)
    var i = 0
    while (i < length) {
      a(i) = in.readLong
      i += 1
    }
    a
  }

  def apply(): FeltCubeTreeMask = FeltCubeTreeMask(null)
}

/**
 * provides state information for dimension or aggregation semantics
 *
 * @param data
 */
final case
class FeltCubeTreeMask(data: Array[Long] = null) extends AnyVal {

  override def toString: String = {
    new String(data.flatMap(VitalsBitMapAnyVal(_).toString.getBytes)).replace("0000 ", "").trim
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // code generation
  ///////////////////////////////////////////////////////////////////////////////////
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${classOf[FeltCubeTreeMask].getName}(${generateLongArrayCode(data)(cursor indentRight)}
        |$I)""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////

  def bitMapForCube(cubeId: Int): VitalsBitMapAnyVal = {
    data(cubeId)
  }

  def addCube(cubeId: Int, view: VitalsBitMapAnyVal): Unit = {
    data(cubeId) = view.data
  }

  def addCube(cubeId: Int, join: ArrayBuffer[Int]): Unit = {
    var i = 0
    var d = 0L
    while (i < join.length) {
      d = VitalsBitMapAnyVal(d).setBit(join(i))
      i += 1
    }
    data(cubeId) = d
  }

  def addCube(cubeId: Int, join: Array[Int]): Unit = {
    var i = 0
    var d = 0L
    while (i < join.length) {
      d = VitalsBitMapAnyVal(d).setBit(join(i))
      i += 1
    }
    data(cubeId) = d
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE (ENCODING)
  ///////////////////////////////////////////////////////////////////////////////////

  /**
   * serialize this mask
   *
   * @param kryo
   * @param out
   */
  def write(kryo: Kryo, out: Output): Unit = {
    out writeByte data.length
    var i = 0
    while (i < data.length) {
      out writeLong data(i)
      i += 1
    }
  }

}

