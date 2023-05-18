/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker

import org.burstsys.fabric.wave.data.model.store.{FabricStoreRegistry, storeProviders}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

package object store extends VitalsLogger {

  /**
   * the singleton registry for worker stores within the local JVM
   */
  private[this]
  final val _workerStoreRegistry = FabricStoreRegistry[FabricStoreWorker]()

  private[this]
  var _loaded = false

  /**
   * load the store providers from the class path
   */
  final
  def startWorkerStores(container: FabricWaveWorkerContainer): Unit = {
    synchronized {
      if (_loaded) return
      storeProviders.foreach {
        provider =>
          try {
            log info s"STORE_PROVIDER_WORKER_START: storeName='${provider.storeName}'"
            val constructor = try {
              provider.workerClass.getConstructor(classOf[FabricWaveWorkerContainer])
            } catch safely {
              case t:Throwable =>
                log error burstStdMsg(s"STORE_PROVIDER_WORKER_BAD_CONSTRUCTOR storeName='${provider.storeName}' $t", t)
                throw t
            }
            val workerStore = constructor.newInstance(container).asInstanceOf[FabricStoreWorker]
            _workerStoreRegistry.addStore(workerStore)
            workerStore.start
            container.health.registerService(workerStore)
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"STORE_PROVIDER_WORKER_START_FAIL storeName='${provider.storeName}' $t", t)
          }
      }
      _loaded = true
    }
  }

  final
  def stopWorkerStores(container: FabricWaveWorkerContainer): Unit = {
    synchronized {
      _workerStoreRegistry.stop
      _loaded = false
    }
  }

  final
  def getWorkerStore(datasource: FabricDatasource): FabricStoreWorker = {
    _workerStoreRegistry.getOrThrow(datasource)
  }

}
