/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.runtime

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioTypes.BrioRelationName
import org.burstsys.felt.model.collectors.cube.runtime
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeOrdinalMap.OrdinalMap
import org.burstsys.felt.model.tree.code._
import gnu.trove.map.hash.TObjectIntHashMap
import gnu.trove.procedure.TObjectIntProcedure

import scala.language.implicitConversions


/**
 * a specialized kryro serializable helper type to store a field-name to cube column (dimension or aggregation) ordinal key
 * (GIST ONLY)
 */

object FeltCubeOrdinalMap {

  type OrdinalMap = TObjectIntHashMap[BrioRelationName]

  implicit def mapToOrdinalMap(data: OrdinalMap): FeltCubeOrdinalMap = runtime.FeltCubeOrdinalMap(data)

  implicit def ordinalMapToMap(view: FeltCubeOrdinalMap): OrdinalMap = view.data

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE (DECODING)
  ///////////////////////////////////////////////////////////////////////////////////
  def apply(kryo: Kryo, in: Input): FeltCubeOrdinalMap = {
    val length = in.readByte
    val data = new OrdinalMap
    var i = 0
    while (i < length) {
      data.put(in.readString, in.readInt)
      i += 1
    }
    data
  }

}

/**
 * cube column name to ordinal map (GIST ONLY)
 */
final case
class FeltCubeOrdinalMap(data: OrdinalMap = new OrdinalMap) extends AnyVal {

  override def toString: String = data.toString

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${classOf[FeltCubeOrdinalMap].getName}(
        |${I2}new ${classOf[TObjectIntHashMap[BrioRelationName]].getName}()
        |$I)""".stripMargin
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
    out writeByte data.size
    data.forEachEntry(
      new TObjectIntProcedure[String] {
        override def execute(key: String, value: Int): Boolean = {
          out writeString key
          out writeInt value
          true
        }
      }
    )
  }


}

