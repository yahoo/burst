/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.provider

import org.burstsys.catalog.model.account._
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog._
import org.burstsys.relate.RelatePk
import scalikejdbc.DBSession

import scala.util.Success
import scala.util.Try

trait CatalogAccountReactor extends CatalogService {

  self: CatalogServiceContext =>

  private def onlyOnServer[T](work: DBSession => T): T = {
    if (modality.isServer) {
      sql.connection.localTx(work(_))
    } else ???
  }

  final override def registerAccount(username: String, password: String): Try[RelatePk] = resultOrFailure {
    onlyOnServer { implicit session =>
      Success(sql.accounts.insertAccount(username, password))
    }
  }

  final override def verifyAccount(username: String, password: String): Try[CatalogAccount] = {
    onlyOnServer { implicit session =>
      sql.accounts.verifyAcount(username, password)
    }
  }

  final override def changeAccountPassword(username: String, oldPassword: String, newPassword: String): Try[Boolean] = {
    onlyOnServer { implicit session =>
      sql.accounts.changePassword(username, oldPassword, newPassword)
    }
  }
}
