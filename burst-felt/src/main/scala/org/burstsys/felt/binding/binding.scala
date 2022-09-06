/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.felt.model.collectors.runtime.FeltCollector
import org.burstsys.felt.model.mutables.FeltMutable
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reflection

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/**
 * ==Felt Provider Bindings==
 * A [[FeltBinding]] is used to connect the Felt analysis tree to various functional
 * implementation choices made outside Felt e.g. [[FeltCollector]]
 * and [[FeltMutable]] support.
 */
package object binding extends VitalsLogger {

  private
  lazy val _bindLookup: Map[String, FeltBinding] = {
    val map = new ConcurrentHashMap[String, FeltBinding]()
    reflection.getSubTypesOf(classOf[FeltBinding]).foreach {
      bindClass =>
        try {
          val binding = bindClass.getDeclaredConstructor().newInstance()
          log info s"FELT_BINDING_MAP_FOUND ${binding.name}"
          map put (binding.name, binding)
        } catch {
          case t: Throwable =>
            log error s"FELT_BINDING_MAP_FAIL ${bindClass.getName} $t"
        }
    }
    map.asScala.toMap
  }

  final def bindingLookup(name: String): FeltBinding = _bindLookup.getOrElse(name,
    throw VitalsException(s"FELT_BINDING_LOOKUP_NOT_FOUND $name")
  )

}
