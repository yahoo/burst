/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.worker

import org.burstsys.fabric
import org.burstsys.fabric.configuration.burstFabricWorkerStandaloneProperty
import org.burstsys.fabric.container.WorkerLog4JPropertiesFileName
import org.burstsys.vitals.git
import org.burstsys.vitals.io.loadSystemPropertiesFromJavaPropertiesFile
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.worker.configuration.burstWorkerPropertiesFileProperty

object BurstWorkerMain {

  final case class BurstWorkerArguments(standalone: Boolean = false)

  def main(args: Array[String]): Unit = {

    var defaultArguments = BurstWorkerArguments()

    val parser = new scopt.OptionParser[BurstWorkerArguments]("BurstWorkerMain") {
      opt[Unit]('s', "standalone") text s"start in standalone development mode (default '${defaultArguments.standalone}')" maxOccurs 1 action {
        case (newValue, arguments) => arguments.copy(standalone = true)
      }
      help("help")
    }

    parser.parse(args.toSeq, defaultArguments) match {
      case None =>
        parser.showUsageAsError()
        System.exit(-1)
      case Some(arguments) =>
        if (arguments.standalone) {
          burstFabricWorkerStandaloneProperty.set(true)
          git.turnOffBuildValidation()
        }

        /**
         * first get a set of basic config properties - this will eventually just be catalog DB connection info...
         */
        VitalsLog.configureLogging(WorkerLog4JPropertiesFileName)
        loadSystemPropertiesFromJavaPropertiesFile(burstWorkerPropertiesFileProperty.getOrThrow)
        fabric.container.workerContainer.start.run.stop
    }

  }

}
