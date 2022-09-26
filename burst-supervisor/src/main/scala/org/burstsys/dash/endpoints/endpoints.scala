/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging.VitalsLogger

package object endpoints extends VitalsLogger {

  /**
   * A marker trait for all JSON responses that get serialized to the dashboard
   */
  trait ClientJsonObject extends VitalsJsonObject

  /**
   * A marker trait for all payloads that can be sent over a websocket
   */
  trait ClientWebsocketMessage extends ClientJsonObject {
    def msgType: String
  }

  type BurstApiName = String

  type BurstMethodName = String

  final val BurstRestUrlBase = "/api/supervisor/"
  final val CacheApiPath = BurstRestUrlBase + "cache"
  final val CatalogApiPath = BurstRestUrlBase + "catalog"
  final val ExecutionApiPath = BurstRestUrlBase + "execution"
  final val FabricApiPath = BurstRestUrlBase + "fabric"
  final val InfoApiPath = BurstRestUrlBase + "info"
  final val ProfilerApiPath = BurstRestUrlBase + "profiler"
  final val QueryApiPath = BurstRestUrlBase + "query"
  final val TorcherApiPath = BurstRestUrlBase + "torcher"

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)


  final case class BurstGenericJsonResult(success: Boolean = true, message: String = "ok") extends ClientJsonObject
}
