/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.net

import com.fasterxml.jackson.databind.json.JsonMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.burstsys.vitals.json

/**
 * A simple base class that can be used to define simple request handlers
 */
case class SimpleServerHandler() extends HttpHandler {

  protected val mapper: JsonMapper = json.buildJsonMapper

  override def handle(exchange: HttpExchange): Unit = {
    val method = exchange.getRequestMethod.toUpperCase
    val path = exchange.getRequestURI.getPath
    method match {
      case "GET" =>
        doGet(path, exchange)
      case "POST" =>
        doPost(path, exchange)
      case _ =>
        exchange.sendResponseHeaders(405, -1)
    }
    exchange.close()
  }

  protected def doGet(path: String, exchange: HttpExchange): Unit = {
    exchange.sendResponseHeaders(405, -1)
  }

  protected def doPost(path: String, exchange: HttpExchange): Unit = {
    exchange.sendResponseHeaders(405, -1)
  }

  /**
   * Send a json payload. Sets the status code and response size, as well as setting Content-Type: application/json.
   * @param exchange the current http exchange
   * @param body the json response body
   * @param code the status code to send, default to 200
   */
  protected def writeJSON(exchange: HttpExchange, body: String, code: Int = 200): Unit = {
    val bytes = body.getBytes("UTF-8")
    exchange.getResponseHeaders.add("Content-Type", "application/json")
    exchange.sendResponseHeaders(code, bytes.length)
    exchange.getResponseBody.write(bytes)

  }

}
