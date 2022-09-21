/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.brio.model.schema.registerBrioSchema
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection

package object provider extends VitalsLogger {

  private lazy val schemaProviders: Array[BrioSchemaProvider] = {
    val scannedClasses = reflection.getSubTypesOf(classOf[BrioSchemaProvider])
    scannedClasses.map(_.getDeclaredConstructor().newInstance()).toArray
  }

  private[this] var _loaded = false

  /**
   * load the schema providers from the class path
   */
  final def loadBrioSchemaProviders(): Unit = {
    synchronized {
      if (_loaded) return
      schemaProviders.foreach { provider =>
        try {
          log info s"BRIO_SCHEMA_PROVIDER_LOAD: $provider"
          registerBrioSchema(this.getClass, provider.schemaResourcePath, provider.names.toIndexedSeq: _*)
        } catch safely {
          case t: Throwable =>
            log error burstStdMsg(s"BRIO_SCHEMA_PROVIDER_LOAD_FAIL provider='$provider' $t", t)
        }
      }
      _loaded = true
    }
  }

}
