/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Feature
import jakarta.ws.rs.core.FeatureContext
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.ext.ContextResolver
import jakarta.ws.rs.ext.MessageBodyReader
import jakarta.ws.rs.ext.MessageBodyWriter
import jakarta.ws.rs.ext.Provider
import org.burstsys.vitals
import org.glassfish.jersey.CommonProperties

/**
 * This is the way that Jersey gets to know jackson a little better
 */
class FabricHttpJacksonFeature extends Feature {
  def configure(context: FeatureContext): Boolean = {
    val disableMoxy: String = CommonProperties.MOXY_JSON_FEATURE_DISABLE + '.' + context.getConfiguration.getRuntimeType.name.toLowerCase
    context.property(disableMoxy, true)
    context.register(classOf[JacksonJsonProvider], classOf[MessageBodyReader[_]], classOf[MessageBodyWriter[_]])
    true
  }
}

/**
 * install the jackson object mapper into jersey
 */
@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
class FabricHttpJacksonProvider extends ContextResolver[ObjectMapper] {
  def getContext(klass: Class[_]): ObjectMapper = mapper
}
