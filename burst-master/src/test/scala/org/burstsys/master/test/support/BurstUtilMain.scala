/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.support

import org.burstsys.catalog.CatalogService.CatalogMasterConfig
import org.burstsys.catalog.CatalogUtilManager
import org.burstsys.catalog.configuration.burstCatalogDbHostProperty
import org.burstsys.catalog.configuration.burstCatalogDbPasswordProperty
import org.burstsys.catalog.configuration.burstCatalogDbUserProperty
import org.burstsys.master.configuration.burstMasterPropertiesFileProperty
import org.burstsys.vitals.configuration.burstCellNameProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.io.loadSystemPropertiesFromJavaPropertiesFile
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.burstStdMsg

/**
 * Burst Util Main is the entry point for the Command Line Interface for the system.  It is used for
 * initialization of a new system and for scripting
 * <br><br>
 *
 * *  <strong>Initialize a New System</strong>
 * <br><br>
 * 1)  Initialize the Mysql DBMS with all the tables needed for the catalog:<pre>
 *
 * {@code BurstUtilMain catalog [-h host] [-u user] [-p password] setup schema [--dropTables]}
 *
 * {@code --dropTables} will drop any preexisting tables
 * {@code -h|--dbHost} will change the target host (default = localhost}
 * {@code -u|--dbUser} will change the db user (default = burst}
 * {@code -u|--dbPassword} will change the db password (default = burst}
 *
 * </pre>
 * The schema {@code burst_catalog} is created and the necessary tables are created
 *
 * <br><br>
 * 2)  Initialize the site and cell entries: <pre>
 *
 * {@code BurstUtilMain catalog [-h host] [-u user] [-p password] setup cell <cellName>}
 *
 * </pre>
 * An cell entry for {@code cellName} is added to the cell table and it becomes part of the {@code default}
 * site.  The {@code default} site is created if it doesn't exist already.
 */

object BurstUtilMain {

  final case class BurstUtilArguments(
                                       /* sql connection info*/
                                       dbHost: String = burstCatalogDbHostProperty.getOrThrow,
                                       dbUser: String = burstCatalogDbUserProperty.getOrThrow,
                                       dbPassword: String = burstCatalogDbPasswordProperty.getOrThrow,
                                       dbDropTables: Boolean = false,
                                       /* command */
                                       command: String = "",
                                       /* cell */
                                       cellName: String = burstCellNameProperty.getOrThrow
                                     )

  def main(args: Array[String]): Unit = {

    val defaultArguments = BurstUtilArguments()

    loadSystemPropertiesFromJavaPropertiesFile(burstMasterPropertiesFileProperty.getOrThrow)

    val parser = new scopt.OptionParser[BurstUtilArguments]("BurstUtilMain") {
      cmd("catalog")
        .action((_, c) => c.copy(command = "catalog"))
        .text("These are the catalog management commands.")
        .children(
          cmd("setup")
            .maxOccurs(1)
            .action((_, c) => c.copy(command = c.command + "_setup"))
            .text("catalog setup commands.").children(
            cmd("schema")
              .hidden()
              .action((_, c) => c.copy(command = c.command + "_schema"))
              .text("create schema and tables."),
            cmd("cell")
              .action((_, c) => c.copy(command = c.command + "_cell"))
              .text("create a new cell in catalog.")
              .children(
                arg[String]("<cell>")
                  .maxOccurs(1)
                  .action((cellName, c) => c.copy(cellName = cellName))
                  .text("Cell name")
              )
          ),
          opt[String]("dbHost")
            .abbr("h")
            .action((host, c) => c.copy(dbHost = host))
            .text("SQL connection host"),
          opt[String]("dbUser")
            .maxOccurs(1)
            .abbr("u")
            .action((user, c) => c.copy(dbUser = user))
            .text("SQL connection user"),
          opt[String]("dbPassword")
            .maxOccurs(1)
            .abbr("p")
            .action((passwd, c) => c.copy(dbPassword = passwd))
            .text("SQL connection password"),
          opt[Unit]("dropTables")
            .maxOccurs(1)
            .action((_, c) => c.copy(dbDropTables = true))
            .text("Drop tables before creating new ones")
        )
      help("help")
    }

    VitalsLog.configureLogging("Util", consoleOnly = true)
    parser.parse(args.toSeq, defaultArguments) match {
      case None =>
        parser.showUsageAsError()
        System.exit(-1)
      case Some(arguments) =>
        burstCatalogDbHostProperty.set(arguments.dbHost)
        burstCatalogDbUserProperty.set(arguments.dbUser)
        burstCatalogDbPasswordProperty.set(arguments.dbPassword)
        burstCellNameProperty.set(arguments.cellName)
        arguments.command match {
          case "catalog_setup_schema" =>
            try {
              val util = CatalogUtilManager(CatalogMasterConfig)
              util.createTables(arguments.dbDropTables)
              util.cleanUp()
              log info burstStdMsg("Schema setup complete")
            } catch safely {
              case e: Exception =>
                log error burstStdMsg("unable to setup schema", e)
                System.exit(-1)
            }
          case "catalog_setup_cell" =>
            try {
              val util = CatalogUtilManager(CatalogMasterConfig)
              util.createCell(arguments.cellName)
              util.cleanUp()
              log info burstStdMsg(s"Cell '${arguments.cellName}' setup complete")
            } catch safely {
              case e: Exception =>
                log error burstStdMsg("unable to setup schema", e)
                System.exit(-1)
            }
          case _ =>
            parser.showUsage()
        }
    }
  }
}