/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}

package object kryo {

  trait FeltKryoSerializable extends Any {
    def write(kryo: Kryo, output: Output): Unit

    def read(kryo: Kryo, input: Input): Unit
  }

}
