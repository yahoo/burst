/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http

import org.burstsys.fabric.container.http.FabricHttpBasicAuthorizer

class SampleStoreHttpAuthorizer() extends FabricHttpBasicAuthorizer {
  override def isPathPublic(path: String): Boolean = path match {
    case "" | "api/view-request" => true
    case viewDetails if path.startsWith("api/view-request") => true
    case path => super.isPathPublic(path)
  }
}
