/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.cli

import org.burstsys.client.util.CsvBuilder
import org.burstsys.client.util.DatumValue
import org.burstsys.gen.thrift.api.client.BTResultStatus

import java.nio.charset.StandardCharsets
import java.util.UUID
import scala.collection.JavaConverters._

class BurstCli {

  /**
   * Build a connection based. This is the primary method subclasses should override.
   * The base implementation of this method attempts to load connection parameters from a named file.
   *
   * @param connStr the name or path of the connection to load
   * @return a Connection to use when talking to Burst
   */
  def getConnection(connStr: String): Connection = {
    Connection.fromFile(connStr)
  }

  def execute(args: BurstCliArgs): Unit = {
    log.info("Getting connection for '{}'", args.connection)
    val conn = getConnection(args.connection)
    args.command match {
      case EnsureDomain => ensureDomain(conn, args.domainArgs)
      case EnsureView => ensureView(conn, args.viewArgs)
      case ExecuteQuery => executeQuery(conn, args.queryArgs)
    }
  }

  private def ensureDomain(connection: Connection, args: EnsureDomainArgs): Unit = {
    log.info("Ensure domain called. source={}", args.source)
    val domain = args.source match {
      case FromFile => Domain.fromFile(args.file)
      case FromText => Domain.fromText(args.json)
    }
    log.info("Ensuring domain: {}", domain)
    val res = connection.client.ensureDomain(domain)
    log.info(s"response status=${res.getOutcome.getStatus} meta=${res.getMeta}")
    log.info("response domain={}", res.getDomain)
  }

  private def ensureView(connection: Connection, args: EnsureViewArgs): Unit = {
    log.info("Ensure view called. source={}", args.source)
    val view = args.source match {
      case FromFile => View.fromFile(args.file)
      case FromText => View.fromText(args.json)
    }
    log.info("Ensuring view: {}", view)
    val res = connection.client.ensureDomainContainsView(view.domainUdk, view)
    log.info(s"response status=${res.getOutcome.getStatus} meta=${res.getMeta}")
    log.info("response view={}", res.getView)
  }

  private def executeQuery(connection: Connection, args: ExecuteQueryArgs): Unit = {
    log.info("Execute query called")
    val guid = s"CLI_${UUID.randomUUID().toString.replace("-", "")}"
    val query = args.querySource match {
      case FromFile => new String(getFileBytes(args.queryFile), StandardCharsets.UTF_8)
      case FromText => args.queryText
    }
    val params = args.paramsSource match {
      case FromFile => Parameters.fromFile(args.paramsFile)
      case FromText => Parameters.fromText(args.paramsJson)
    }
    val res = connection.client.executeQuery(guid, args.domain, args.view, query, args.timezone, params)
    log.info(s"response status=${res.getOutcome.getStatus} meta=${res.getMeta}")

    res.getOutcome.getStatus match {
      case BTResultStatus.SuccessStatus | BTResultStatus.NoDataStatus =>
        val result = res.getResult
        log.info(CsvBuilder.allResults(result))
      case _ =>
    }
  }

  private def csvRow(row: TraversableOnce[_]): String =
    row
      .map(col => col.toString.replace(",", "\\,"))
      .mkString("", ",", "\n")

}
