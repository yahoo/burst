/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.burstsys.dash.configuration.burstRestPortProperty

import jakarta.ws.rs.container.{ContainerRequestContext, ContainerResponseContext, ContainerResponseFilter}
import jakarta.ws.rs.ext.Provider

/**
 * Adds CORS headers to requests
 */
@Provider
class BurstDashCorsFilter extends ContainerResponseFilter {

  private val portNum = burstRestPortProperty.getOrThrow

  override def filter(request: ContainerRequestContext, response: ContainerResponseContext): Unit = {
    response.getHeaders.add("Access-Control-Allow-Origin", s"https://localhost:$portNum")
    response.getHeaders.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
    response.getHeaders.add("Access-Control-Allow-Credentials", "true")
  }

}
