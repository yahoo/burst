/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.felt.model.mutables.valset.FeltMutableValSetBuilder
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder

/**
 * parameters for building a zap val set
 */
final case
class ZapValSetBuilder(
                        var name: String = "no_name",
                        var defaultStartSize: TeslaMemorySize = flex.defaultStartSize
                      ) extends FeltMutableValSetBuilder with KryoSerializable {

  def this() = this(null, 0)

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(name)
    output.writeInt(defaultStartSize)
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    name = input.readString
    defaultStartSize = input.readInt
  }

}
