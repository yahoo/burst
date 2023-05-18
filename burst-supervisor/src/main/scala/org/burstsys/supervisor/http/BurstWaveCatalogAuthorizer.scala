/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http

import org.burstsys.catalog.CatalogService
import org.burstsys.fabric.container.http.FabricHttpBasicAuthorizer
import org.burstsys.fabric.container.http.User

import scala.util.Try

class BurstWaveCatalogAuthorizer(catalog: CatalogService) extends FabricHttpBasicAuthorizer {

  override protected def checkUser(user: String, password: String): Try[User] =
    catalog.verifyAccount(user, password) map { account =>
      val roles = account.labels
        .flatMap(_.get("roles"))
        .map(_.split(","))
        .getOrElse(Array.empty)
      new User(account.moniker, roles)
    }

  /**
   * Allow public access to the thrift endpoint
   * @param path the path to check
   * @return if public access should be allowed
   */
  override def isPathPublic(path: String): Boolean = path match {
    case "thrift/client" => true
    case path => super.isPathPublic(path)
  }
}
