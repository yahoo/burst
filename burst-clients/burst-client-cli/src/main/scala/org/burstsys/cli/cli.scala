/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.THttpClient
import org.burstsys.client.util.DatumValue
import org.burstsys.client.util.ParameterBuilder
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTDataFormat
import org.burstsys.gen.thrift.api.client.BTDataType
import org.burstsys.gen.thrift.api.client.domain.BTDomain
import org.burstsys.gen.thrift.api.client.query.BTCell
import org.burstsys.gen.thrift.api.client.query.BTParameter
import org.burstsys.gen.thrift.api.client.view.BTView
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.reflect.classTag

package object cli {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  val mapper: JsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

  sealed trait Command

  final case object EnsureDomain extends Command

  final case object EnsureView extends Command

  final case object ExecuteQuery extends Command

  sealed trait Source

  final case object FromFile extends Source

  final case object FromText extends Source

  final case class EnsureDomainArgs
  (
    file: String = "",
    json: String = ""
  ) {
    def error: String = {
      if (file == "" && json == "") {
        "you must provide either a domain definition or a domain definition file"
      } else ""
    }

    def source: Source =
      if (file != "") FromFile
      else FromText
  }

  final case class EnsureViewArgs
  (
    file: String = "",
    json: String = ""
  ) {
    def error: String = {
      if (file == "" && json == "") {
        "you must provide either a domain definition or a domain definition file"
      } else ""
    }

    def source: Source =
      if (file != "") FromFile
      else FromText
  }

  final case class ExecuteQueryArgs
  (
    domain: String = "",
    view: String = "",
    queryFile: String = "",
    queryText: String = "",
    timezone: String = "UTC",
    paramsFile: String = "",
    paramsJson: String = "[]" // default to [] because if no file is provided, we try to parse this as json
  ) {
    def error: String = {
      if (domain == "") {
        "you must specify a domain"
      } else if (view == "") {
        "you must specify a view"
      } else if (queryFile == "" && queryText == "") {
        "you must provide query text or a query file"
      } else ""
    }

    def querySource: Source =
      if (queryFile != "") FromFile
      else FromText

    def paramsSource: Source =
      if (paramsFile != "") FromFile
      else FromText
  }

  final case class BurstCliArgs
  (
    connection: String = "",
    command: Command = null,
    domainArgs: EnsureDomainArgs = EnsureDomainArgs(),
    viewArgs: EnsureViewArgs = EnsureViewArgs(),
    queryArgs: ExecuteQueryArgs = ExecuteQueryArgs()
  )

  /* *************************************
   * JSON/Thrift Marshaling
   * *************************************/

  final case class Connection
  (
    name: String,
    host: String,
    port: Int,
    path: String = "/thrift/client",
    secure: Boolean = true
  ) {
    def client: BTBurstService.Client = {
      val protocol = if (secure) "https" else "http"
      val thriftEndpoint = "%s://%s:%d%s".format(protocol, host, port, path)
      log.info(s"Creating Thrift HTTP client name=${name} url=${thriftEndpoint}")
      val transport = new THttpClient(thriftEndpoint);
      new BTBurstService.Client(new TBinaryProtocol(transport));
    }
  }


  class Loader[T: ClassTag] {
    def fromFile(path: String): T = fromBytes(getFileBytes(path))

    def fromText(json: String): T = fromBytes(getStringBytes(json))

    def fromBytes(bytes: Array[Byte]): T = mapper.readValue(bytes, classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  def getFileBytes(path: String): Array[Byte] = Files.readAllBytes(new File(path).toPath)

  def getResource(resource: String): InputStream = getClass.getResourceAsStream(resource)

  def getStringBytes(json: String): Array[Byte] = json.getBytes(StandardCharsets.UTF_8)

  final object Connection extends Loader[Connection]

  final object Domain extends Loader[BTDomain]

  final object View extends Loader[BTView]

  final object Parameters extends Loader[util.List[BTParameter]] {
    override def fromBytes(bytes: Array[Byte]): util.List[BTParameter] = {
      val params = mapper.readValue(bytes, classOf[Array[Map[String, Any]]])
      params.map(p => {
        val name = p("name").asInstanceOf[String]
        val datum = p("datum")
        p("primaryType").asInstanceOf[Int] match {
          case 0 /*Boolean*/ => ParameterBuilder.build(name, datum.asInstanceOf[Boolean])
          case 1 /*Byte*/ => ParameterBuilder.build(name, s"$datum".toByte.asInstanceOf[java.lang.Byte])
          case 2 /*Short*/ => ParameterBuilder.build(name, s"$datum".toShort.asInstanceOf[java.lang.Short])
          case 3 /*Int*/ => ParameterBuilder.build(name, s"$datum".toInt.asInstanceOf[java.lang.Integer])
          case 4 /*Long*/ => ParameterBuilder.build(name, s"$datum".toLong.asInstanceOf[java.lang.Long])
          case 5 /*Double*/ => ParameterBuilder.build(name, datum.asInstanceOf[Double])
          case 6 /*String*/ => ParameterBuilder.build(name, datum.asInstanceOf[String])
        }
      }).toList.asJava
    }
  }

}
