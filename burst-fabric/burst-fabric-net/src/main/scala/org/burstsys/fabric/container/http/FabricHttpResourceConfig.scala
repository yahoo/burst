/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import org.burstsys.fabric.container.FabricContainer
import org.glassfish.jersey.server.ResourceConfig

class FabricHttpResourceConfig(container: FabricContainer) extends ResourceConfig {

  registerInstances(container.httpBinder)

  register(classOf[FabricHttpJacksonFeature])
  register(classOf[FabricHttpJacksonProvider])
  register(classOf[FabricHttpCorsFilter])
  register(classOf[FabricHttpSecurityFilter])
  register(classOf[FabricHttpExceptionMapper])

  registerClasses(container.httpResources: _*)
}
