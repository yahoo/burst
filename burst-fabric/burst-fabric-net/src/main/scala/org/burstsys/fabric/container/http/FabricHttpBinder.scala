/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import org.burstsys.fabric.container.FabricContainer
import org.burstsys.vitals.healthcheck.VitalsSystemHealthService
import org.glassfish.hk2.api.Factory
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder

class FabricHttpBinder(container: FabricContainer) extends AbstractBinder {

  protected def authorizer: FabricAuthorizationProvider = new FabricHttpBasicAuthorizer()

  override def configure(): Unit = {
    bind(container.health).to(classOf[VitalsSystemHealthService])
    bind(authorizer).to(classOf[FabricAuthorizationProvider])
  }

  protected def bindFactory[T](supplier: () => T): ServiceBindingBuilder[T] = {
    bindFactory(new Factory[T] {
      override def provide(): T = supplier()

      override def dispose(instance: T): Unit = {}
    })
  }

}
