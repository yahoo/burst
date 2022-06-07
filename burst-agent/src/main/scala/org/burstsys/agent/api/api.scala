/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.agent.configuration.AgentApiProperties
import org.burstsys.api.BurstApi
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality

package object api {

  trait AgentApi extends VitalsService with BurstQueryApiService.FutureIface with BurstApi with AgentApiProperties {

    final override def apiName: String = s"agent($modality)"

  }

}
