/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import org.burstsys.fabric

/**
 * Adds CORS headers to requests
 */
@Provider
class FabricHttpCorsFilter extends ContainerResponseFilter {

  private val portNum = fabric.configuration.burstHttpPortProperty.get
  private val allowOrigin = s"https://localhost:$portNum"

  override def filter(request: ContainerRequestContext, response: ContainerResponseContext): Unit = {
    response.getHeaders.add("Access-Control-Allow-Origin", allowOrigin)
    response.getHeaders.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
    response.getHeaders.add("Access-Control-Allow-Credentials", "true")
  }

}
