/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.cli

object BurstCliMain {

  def parser: scopt.OptionParser[BurstCliArgs] = new scopt.OptionParser[BurstCliArgs]("burst-cli") {
    help("help").text("displays this message")

    opt[String]("connection")
      .text("how to connect to the burst master")
      .maxOccurs(1)
      .withFallback(() => "connection.json")
      .action {
        case (c, args) => args.copy(connection = c)
      }
    cmd(EnsureDomain.toString)
      .text("Ensure that a domain matching the provided domain exists")
      .action({ case (_, args) => args.copy(command = EnsureDomain) })
      .children(
        opt[String]('f', "file")
          .text("a domain definition that should exist")
          .action({ case (path, args) => args.copy(domainArgs = args.domainArgs.copy(file = path)) }),
        opt[String]('d', "domain")
          .text("a json definition of the domain that should exist")
          .action({ case (json, args) => args.copy(domainArgs = args.domainArgs.copy(json = json)) }),
        checkConfig(args => {
          if (args.command != EnsureDomain) success
          else args.domainArgs.error match {
            case "" | null => success
            case err => failure(err)
          }
        })
      )
    cmd(EnsureView.toString)
      .text("Ensure that a view matching the provided view exists")
      .action({ case (_, args) => args.copy(command = EnsureView) })
      .children(
        opt[String]('f', "file")
          .text("a domain definition that should exist")
          .action({ case (path, args) => args.copy(viewArgs = args.viewArgs.copy(file = path)) }),
        opt[String]('v', "view")
          .text("a json definition of the view that should exist")
          .action({ case (json, args) => args.copy(viewArgs = args.viewArgs.copy(json = json)) }),
        checkConfig(args => {
          if (args.command != EnsureView) success
          else args.viewArgs.error match {
            case "" | null => success
            case err => failure(err)
          }
        })
      )
    cmd(ExecuteQuery.toString)
      .text("Execute a query, you can provide the query and any required parameters either directly as text, or using files")
      .action({ case (_, args) => args.copy(command = ExecuteQuery) })
      .children(
        opt[String]('d', "domain").required()
          .text("the udk for the domain to query")
          .action({ case (udk, args) => args.copy(queryArgs = args.queryArgs.copy(domain = udk)) }),
        opt[String]('v', "view").required()
          .text("the udk for the view to query")
          .action({ case (udk, args) => args.copy(queryArgs = args.queryArgs.copy(view = udk)) }),
        opt[String]("query")
          .abbr("q")
          .text("the query text to execute")
          .action({ case (query, args) => args.copy(queryArgs = args.queryArgs.copy(queryText = query)) }),
        opt[String]("queryFile")
          .abbr("qf")
          .text("a file containing the query to execute")
          .action({ case (path, args) => args.copy(queryArgs = args.queryArgs.copy(queryFile = path)) }),
        opt[String]('t', "timezone")
          .text("the timezone to use for the query")
          .action({ case (tz, args) => args.copy(queryArgs = args.queryArgs.copy(timezone = tz)) }),
        opt[String]("params")
          .abbr("p")
          .text("the params to include with the query")
          .action({ case (params, args) => args.copy(queryArgs = args.queryArgs.copy(paramsJson = params)) }),
        opt[String]("paramsFile")
          .abbr("pf")
          .text("a file containing the params to include")
          .action({ case (path, args) => args.copy(queryArgs = args.queryArgs.copy(paramsFile = path)) }),
        checkConfig(args =>
          if (args.command != ExecuteQuery) success
          else args.queryArgs.error match {
            case "" | null => success
            case err => failure(err)
          })
      )

    checkConfig(args => {
      if (args.command == null) {
        failure("you must specify a command")
      } else
        success
    })
  }


  def main(args: Array[String]): Unit = {
    parser.parse(args, BurstCliArgs()) match {
      case Some(args) => new BurstCli().execute(args)
      case None => log.warn("Could not parse invocation: {}", args)
    }
  }
}
