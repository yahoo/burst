/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.dash.configuration
import org.burstsys.vitals.logging.VitalsLogger
import org.scalatest.matchers.should.Matchers
import requests.{RequestAuth, RequestBlob, Response}

import java.io.OutputStream
import jakarta.ws.rs.core.MediaType

import scala.jdk.CollectionConverters._

package object rest extends Matchers with VitalsLogger {

  val port: Int =  configuration.burstRestPortProperty.get
  val basicAuth: RequestAuth.Basic = RequestAuth.implicitBasic("burst", "burstomatic")
  val jsonMapper: ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def urlFor(endpoint: String): String = s"https://localhost:$port/api/supervisor$endpoint"

  def fetchArrayFrom(endpoint: String, method: String = "GET"): List[JsonNode] = {
    val json = fetchObjectFrom(endpoint, method)
    json.isArray shouldBe true

    json.asInstanceOf[ArrayNode].iterator.asScala.toList
  }

  def fetchObjectFrom(endpoint: String, method: String = "GET", params: Map[String, String] = Map.empty): JsonNode = {
    val response = method match {
      case "GET" => requests.get(urlFor(endpoint), auth = basicAuth, verifySslCerts = false)
      case "POST" =>
        val data = if (params.isEmpty) RequestBlob.EmptyRequestBlob else RequestBlob.FormEncodedRequestBlob(params)
        requests.post(urlFor(endpoint), auth = basicAuth, data = data, verifySslCerts = false)
    }
    response.statusCode shouldBe 200

    jsonMapper.readTree(response.text())
  }

  def sendFormDataTo(endpoint: String, data: Map[String, Any]): JsonNode = {
    val formData = RequestBlob.FormEncodedRequestBlob(data.map { entry => (entry._1, entry._2.toString) })
    val response = requests.post(urlFor(endpoint), data = formData, auth = basicAuth, verifySslCerts = false)
    response.statusCode shouldBe 200

    jsonMapper.readTree(response.text())
  }

  def sendJsonDataTo(endpoint: String, data: Map[String, Any]): JsonNode = {
    val response: Response = requests.post(urlFor(endpoint), data = JsonRequestBlob(data), auth = basicAuth, verifySslCerts = false)
    response.statusCode shouldBe 200

    jsonMapper.readTree(response.text())
  }

  implicit class JsonRequestBlob(data: Map[String, Any]) extends RequestBlob {
    val serialized: Array[Byte] = jsonMapper.writeValueAsString(data).getBytes

    override def write(out: OutputStream): Unit = out.write(serialized)

    override def headers: Seq[(String, String)] = super.headers ++ Seq(
      "Content-Type" -> MediaType.APPLICATION_JSON
    )
  }

  def assertJsonContains(json: JsonNode, properties: Map[String, Any]): Unit = {
    properties.foreach { entry =>
      val (key, value) = entry
      if (value == null) json.get(key).isNull shouldEqual true
      else value match {
        case _: Long => json.get(key).asLong shouldEqual value
        case _: Int => json.get(key).asInt shouldEqual value
        case _: String => json.get(key).asText shouldEqual value
        case _: Boolean => json.get(key).asBoolean shouldEqual value
      }
    }
  }

}
