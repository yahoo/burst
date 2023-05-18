/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy

import org.burstsys.alloy.alloy.store.supervisor.AlloyJsonStoreSupervisor
import org.burstsys.alloy.alloy.store.worker.AlloyJsonStoreWorker
import org.burstsys.fabric.wave.data.model.store.FabricStoreProvider

package object store {

  final val AlloyJsonStoreName = "synthetic-json"

  final case class AlloyJsonStoreProvider() extends FabricStoreProvider[AlloyJsonStoreSupervisor, AlloyJsonStoreWorker] {

    val storeName: String = AlloyJsonStoreName

    val supervisorClass: Class[AlloyJsonStoreSupervisor] = classOf[AlloyJsonStoreSupervisor]

    val workerClass: Class[AlloyJsonStoreWorker] = classOf[AlloyJsonStoreWorker]
  }

}
