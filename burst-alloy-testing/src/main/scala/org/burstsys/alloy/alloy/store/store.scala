/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy

import org.burstsys.alloy.alloy.store.master.AlloyJsonStoreMaster
import org.burstsys.alloy.alloy.store.worker.AlloyJsonStoreWorker
import org.burstsys.fabric.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.data.model.store.FabricStoreProvider
import org.burstsys.fabric.metadata.model.FabricViewKey

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

package object store {

  final val AlloyJsonStoreName = "alloy-json"

  final case class AlloyJsonStoreProvider() extends FabricStoreProvider[AlloyJsonStoreMaster, AlloyJsonStoreWorker] {

    val storeName: String = AlloyJsonStoreName

    val masterClass: Class[AlloyJsonStoreMaster] = classOf[AlloyJsonStoreMaster]

    val workerClass: Class[AlloyJsonStoreWorker] = classOf[AlloyJsonStoreWorker]
  }

}
