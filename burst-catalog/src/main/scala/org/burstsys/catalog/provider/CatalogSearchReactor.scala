/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.catalog._
import org.burstsys.vitals.errors._

import scala.collection.mutable
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * Used by the dashboard to populate the catalog tab
  */
trait CatalogSearchReactor extends CatalogService {

  self: CatalogServiceContext =>

  final override
  def searchCatalog(domainIdentifier: Option[String], viewPkOrMoniker: Option[String], limit: Option[Int]): Try[Array[Map[String, String]]] = {
    if (modality.isClient)
      return Failure(new RuntimeException("Cannot invoke this as a client"))

    try {
      sql.connection localTx {
        implicit session =>
          val (d, v) = (sql.domains, sql.views)
          val params: mutable.ArrayBuffer[Any] = mutable.ArrayBuffer()

          var whereClause =
            s"""WHERE ${
              domainIdentifier.map(
                domain => Try(domain.toLong) match {
                  case Success(pk) =>
                    params += pk
                    s"d.${d.entityPkColumn} = ?"
                  case Failure(_) =>
                    if (!domain.equalsIgnoreCase("all")) {
                      params ++= Array(s"%$domain%", s"%$domain%")
                      s"LOWER(d.${d.monikerColumn}) LIKE LOWER(?) OR LOWER(d.${d.udkColumn}) LIKE LOWER(?)"
                    } else s""
                }).getOrElse("")
            } ${if (domainIdentifier.isDefined && viewPkOrMoniker.isDefined) "AND" else ""} ${
              viewPkOrMoniker.map(
                view => Try(view.toLong) match {
                  case Success(pk) =>
                    params += pk
                    s"v.${v.entityPkColumn} = ?"
                  case Failure(_) =>
                    if (!view.equalsIgnoreCase("all")) {
                      params ++= Array(s"%$view%", s"%$view%")
                      s"LOWER(v.${v.monikerColumn}) LIKE LOWER(?) OR LOWER(v.${v.udkColumn}) LIKE LOWER(?)"
                    } else s""
                }
              ).getOrElse("")
            }"""

          if (whereClause.trim == "WHERE") whereClause = ""

          val limitClause = configuration.dialect.limitClause(limit)
          params ++= limitClause.parameters

          val query =
            s"""SELECT
               |  d.${d.entityPkColumn} AS domain_pk,
               |  d.${d.monikerColumn} AS domain_moniker,
               |  d.${d.udkColumn} AS domain_udk,
               |  v.${v.entityPkColumn} AS view_pk,
               |  v.${v.monikerColumn} AS view_moniker,
               |  v.${v.udkColumn} AS view_udk
               |FROM ${d.tableName} d
               |  LEFT JOIN ${v.tableName} v ON v.${v.domainFkColumn} = d.${d.entityPkColumn}
               |$whereClause
               |${limitClause.value}
               |""".stripMargin.trim

          log info s"executing search:\n$query\nparams: $params"
          Success(
            session.list(query, {params.toSeq}: _*)({ rs =>
              Map(
                "domain_pk" -> rs.string("domain_pk"),
                "domain_moniker" -> rs.string("domain_moniker"),
                "domain_udk" -> rs.string("domain_udk"),
                "view_pk" -> rs.string("view_pk"),
                "view_moniker" -> rs.string("view_moniker"),
                "view_udk" -> rs.string("view_udk")
              )
            }).toArray)
      }
    } catch safely {
      case t: Throwable => Failure(t)
    }

  }
}

