/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {

  abstract class AgentQuerySpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll with AgentLanguage {

    VitalsLog.configureLogging("agent", consoleOnly = true)

    override def languagePrefixes(): Array[String] = Array("mock")

    val agentService: AgentService = AgentService(VitalsStandaloneServer)
    val agentClient: AgentService = AgentService()

    override protected
    def beforeAll() {
      log info VitalsPropertyRegistry.logReport
      agentService.start
      agentService.registerLanguage(this)
      agentClient.start
    }

    override protected
    def afterAll() {
      agentService.stop
      agentClient.stop
    }


  }


}
