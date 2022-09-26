/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.supervisor

import org.burstsys.agent.processors.BurstSystemEqlQueryProcessor
import org.burstsys.agent.processors.BurstSystemHydraQueryProcessor
import org.burstsys.hydra.HydraService
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.burstStdMsg

/**
 * The startup of a container is where the horizontal strata of top level
 * services are wired together. All configuration is done using [[org.burstsys.vitals.properties.VitalsPropertySpecification]]
 * instances. All of these have defaults, can be set in the VM command line
 */
trait BurstSystemTestSupervisorStartup extends Any {

  self: BurstSystemTestSupervisorContainerContext =>

  final
  def startForeground: this.type = {
    try {
      /////////////////////////////////////////////////////////////////
      // agent and catalog store/API
      /////////////////////////////////////////////////////////////////

      _agentClient.start
      _agentServer.start

      /////////////////////////////////////////////////////////////////
      // hydra
      /////////////////////////////////////////////////////////////////
      _hydra = HydraService(this).start
      _hydraProcessor = BurstSystemHydraQueryProcessor(_agentServer, _hydra)
      _agentServer.registerLanguage(_hydraProcessor)

      /////////////////////////////////////////////////////////////////
      // eql
      /////////////////////////////////////////////////////////////////
      _eqlProcessor = BurstSystemEqlQueryProcessor(_agentServer, _catalogServer)
      _agentServer.registerLanguage(_eqlProcessor)

      // agent commands
      _agentServer.registerCache(data)

      /////////////////////////////////////////////////////////////////
      // boot
      /////////////////////////////////////////////////////////////////

      this

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$serviceName startup failed", t)
        throw VitalsException(t)
    }
  }

}
