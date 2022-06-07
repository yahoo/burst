/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.vitals.VitalsService
import scalikejdbc.{DBConnection, DBSession}

/**
  * A JDBC relational database persistence service
  */
trait RelateService extends VitalsService {

  /**
    * register a relate persister for this service
    *
    * @param persister
    */
  def registerPersister(persister: RelatePersister[_])

  /**
    * The dialect (the specific RDBMS) for this service
    *
    * @return
    */
  def dialect: RelateDialect

  /**
    * the underlying JDBC connection
    *
    * @return
    */
  def connection: DBConnection

  /**
    * The name of the JDBC database
    *
    * @return
    */
  def dbName: String

  /**
    * The name/ip-address of the JDBC server host
    *
    * @return
    */
  def dbHost: String

  /**
    * The port of the JDBC server
    *
    * @return
    */
  def dbPort: Int

  /**
    * The user of the JDBC database
    *
    * @return
    */
  def dbUser: String

  /**
    * The password of the JDBC database
    *
    * @return
    */
  def dbPassword: String

  /**
    * the current setting for max connections
    *
    * @return
    */
  def dbConnections: Int

  /**
    * should service lifecycle include executing DDL for this service
    * @return
    */
  def executeDDL: Boolean

  /**
    * execute DDL now
    *
    * @param dropIfExists
    * @param session
    * @return
    */
  def executeDdl(dropIfExists: Boolean = false)(implicit session: DBSession): this.type

  /**
    * execute SQL script
    *
    * @param source
    * @param translateEscapes
    * @param session
    * @return
    */
  def executeScript(source: String, translateEscapes: Boolean = false)(implicit session: DBSession): this.type

}
