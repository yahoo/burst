/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}

package object decl {

  /**
   * cube semantics runtime support for dimensions and aggregations
   */
  trait FeltCubeSemRt extends AnyRef with KryoSerializable {

    //////////////////////////////////////////////////////////////////////
    // API
    //////////////////////////////////////////////////////////////////////

    /**
     * unique user friendly name for this semantic type
     *
     * @return
     */
    final def name: String = semanticType.name

    final override def toString: String = name

    /**
     * the type of this semantic operation
     *
     * @return
     */
    def semanticType: FeltSemType

  }

  /**
   * a cube column
   *
   */
  abstract
  class FeltCubeColSem {

    /**
     * the runtime object that implements this semantic definition
     *
     * @return
     */
    def semanticRt: FeltCubeSemRt

    /**
     * the brio type for this semantic
     *
     * @return
     */
    def bType: BrioTypeKey

    /**
     * human friendly name for this column
     *
     * @return
     */
    def columnName: BrioRelationName

    /**
     * this is also used for source generation
     *
     * @return
     */
    final override
    def toString: String =
      s"${getClass.getSimpleName}[${brioDataTypeNameFromKey(bType)}]('$columnName')"
  }

  /**
   * ENUM type for identifying aggregation and dimensional semantics
   */
  abstract
  class FeltSemType(nm: String) extends AnyRef with KryoSerializable {

    ////////////////////////////////////////////////////////
    // PRIVATE STATE
    ////////////////////////////////////////////////////////

    private[this]
    var _name: String = nm

    ////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////

    /**
     * unique user friendly name for this semantic type
     *
     * @return
     */
    final def name: String = _name

    final override def toString: String = name

    ////////////////////////////////////////////////////////
    // KRYO SERDE
    ////////////////////////////////////////////////////////

    override def write(kryo: Kryo, output: Output): Unit = output.writeString(_name)

    override def read(kryo: Kryo, input: Input): Unit = _name = input.readString
  }

}
