/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.pool

import scalikejdbc.{ConnectionPoolFactory, ConnectionPoolSettings}

/**
  * Connection Pool Factory
  *
  * @see [[http://commons.apache.org/dbcp/]]
  */
object RelateCommons2ConnectionPoolFactory extends ConnectionPoolFactory {

  override def apply(
                      url: String,
                      user: String,
                      password: String,
                      settings: ConnectionPoolSettings = ConnectionPoolSettings()
                    ): RelateCommons2ConnectionPool = {
    new RelateCommons2ConnectionPool(url, user, password, settings)
  }

}
