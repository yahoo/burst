/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container

import com.fasterxml.jackson.databind.json.JsonMapper
import org.burstsys.vitals
import org.burstsys.vitals.logging.VitalsLogger

package object http extends VitalsLogger {

  val mapper: JsonMapper = vitals.json.buildJsonMapper

}
