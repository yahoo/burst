/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import java.io.{FileReader, Reader}

import org.burstsys.vitals.git
import org.burstsys.vitals.net.VitalsHostName

import scala.concurrent.duration._

object Parameters {

  final case class TorcherParameters
  (
    master: VitalsHostName = "localhost",
    verbose: Boolean = false,
    duration: Duration = null,
    loadRate: Double = -1, // As fast as possible
    queryRate: Double = -1, // As fast as possible
    parallelism: Int = -1,
    source: Reader = null,
    clientTimeout: Duration = null,
    reportingInterval: Duration = 30 seconds,
    startingIndex: Int = 0,
    endingIndex: Int = 0
  ) {
    override def toString: String =
      s"""
         | parallelism='$parallelism'
         | master='$master'
         | sourceFile=$source
         | duration=${if (duration.length != 0) duration else "once through all datasets"}
         | loadRate=${if (loadRate != 0) s"$loadRate lps" else "unlimited"}
         | queryRate=${if (queryRate != 0) s"$queryRate qps" else "unlimited"}
       """.stripMargin
  }


  /**
    * Parse all the command line parameters
    */
  def parseArguments(args: Array[String]): Option[TorcherParameters] = {
    val torcherParameters = TorcherParameters()

    val parser = new scopt.OptionParser[TorcherParameters]("torcher") {
      head("Torcher", s"branch=${git.branch} commit=${git.commitId}")
      opt[Unit]('v', "verbose") text s"print lots of messages" maxOccurs 1 action {
        (_, parameters) => parameters.copy(verbose = true)
      }
      opt[String]('m', "master") text s"target cell master host name/address" maxOccurs 1 action {
        (newValue, parameters) => parameters.copy(master = newValue)
      }
      opt[Int]('p', "parallelism").text(s"number of datasets to load concurrently").action {
        (newValue, parameters) => parameters.copy(parallelism = newValue)
      }
      opt[Duration]('t', "timeout").text(s"duration to wait for a query/load response")
        .maxOccurs(1).action(
        (newValue, parameters) => parameters.copy(clientTimeout = newValue)
      )
      opt[Duration]('d', "duration") text s"length of test" maxOccurs 1 action {
        (newValue, parameters) => parameters.copy(duration = newValue)
      }
      opt[Duration]("reporting-frequency").abbr("rf") text s"how often intermediary statistics are reported" maxOccurs 1 action {
        (newValue, parameters) => parameters.copy(reportingInterval = newValue)
      }
      opt[Double]("load-rate") abbr "lr" text s"LPS (loads per second)" maxOccurs 1 action {
        (newValue, parameters) => parameters.copy(loadRate = newValue)
      }
      opt[Double]("query-rate") abbr "qr" text s"QPS (queries per second)" maxOccurs 1 action {
        (newValue, parameters) => parameters.copy(queryRate = newValue)
      }
      arg[String]("<file>") text s"file name for dataset specification file" minOccurs 1 action (
        (newValue, parameters) => parameters.copy(source = new FileReader(newValue))
        )
      opt[Int]("start-index").abbr("si").text(s"a start index into the dataset file").maxOccurs(1).action {
        (newValue, parameters) => parameters.copy(startingIndex = newValue)
      }
      opt[Int]("end-index").abbr("ei").text(s"a end index into the dataset file").maxOccurs(1).action {
        (newValue, parameters) => parameters.copy(endingIndex = newValue)
      }
      help("help")
      checkConfig { c =>
        if (c.startingIndex < 0)
          failure("invalid starting index value")

        if (c.endingIndex != 0 && (c.endingIndex < 0 || c.startingIndex >= c.endingIndex))
          failure("invalid ending index value")

        success
      }
    }

    parser.parse(args.toSeq, torcherParameters) match {
      case None =>
        parser.showUsageAsError()
        None
      case Some(p) =>
        Some(p)
    }
  }

}
