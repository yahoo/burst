/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.master

import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.data.model.store.{FabricStoreRegistry, storeProviders}
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

package object store extends VitalsLogger {

  /**
   * the singleton registry for master stores within the local JVM
   */
  private[this]
  final val _masterStoreRegistry = FabricStoreRegistry[FabricStoreMaster]()

  private[this]
  var _loaded = false

  /**
   * load the store providers from the class path
   */
  final
  def startMasterStores(container: FabricMasterContainer): Unit = {
    synchronized {
      if (_loaded) return
      storeProviders.foreach {
        provider =>
          try {
            log info s"STORE_PROVIDER_MASTER_LOAD: storeName='${provider.storeName}'"
            val constructor = try {
              provider.masterClass.getConstructor(classOf[FabricMasterContainer])
            } catch safely {
              case t: Throwable =>
                log error burstStdMsg(s"STORE_PROVIDER_MASTER_BAD_CONSTRUCTOR storeName='${provider.storeName}' $t", t)
                throw t
            }
            val store = constructor.newInstance(container).asInstanceOf[FabricStoreMaster]
            _masterStoreRegistry.addStore(store)
            store.start
            container.health.registerService(store)
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"STORE_PROVIDER_MASTER_LOAD_FAIL storeName='${provider.storeName}' $t", t)
          }
      }
      _loaded = true
    }
  }

  final
  def stopMasterStores(container: FabricMasterContainer): Unit = {
    synchronized {
      log info s"STORE_PROVIDER_MASTER_STOP_ALL"
      _masterStoreRegistry.stop
      _loaded = false
    }
  }

  final
  def getMasterStore(datasource: FabricDatasource): FabricStoreMaster = {
    _masterStoreRegistry.getOrThrow(datasource)
  }

  final
  def getMasterStore(name: String): FabricStoreMaster = {
    _masterStoreRegistry.get(name)
  }

}
