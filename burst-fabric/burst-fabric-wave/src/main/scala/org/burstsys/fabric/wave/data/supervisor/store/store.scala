/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.supervisor

import org.burstsys.fabric.wave.data.model.store.{FabricStoreRegistry, storeProviders}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

package object store extends VitalsLogger {

  /**
   * the singleton registry for supervisor stores within the local JVM
   */
  private[this]
  final val _supervisorStoreRegistry = FabricStoreRegistry[FabricStoreSupervisor]()

  private[this]
  var _loaded = false

  /**
   * load the store providers from the class path
   */
  final
  def startSupervisorStores(container: FabricWaveSupervisorContainer): Unit = {
    synchronized {
      if (_loaded) return
      storeProviders.foreach {
        provider =>
          try {
            log info s"STORE_PROVIDER_SUPERVISOR_LOAD: storeName='${provider.storeName}'"
            val constructor = try {
              provider.supervisorClass.getConstructor(classOf[FabricWaveSupervisorContainer])
            } catch safely {
              case t: Throwable =>
                log error burstStdMsg(s"STORE_PROVIDER_SUPERVISOR_BAD_CONSTRUCTOR storeName='${provider.storeName}' $t", t)
                throw t
            }
            val store = constructor.newInstance(container).asInstanceOf[FabricStoreSupervisor]
            _supervisorStoreRegistry.addStore(store)
            store.start
            container.health.registerService(store)
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"STORE_PROVIDER_SUPERVISOR_LOAD_FAIL storeName='${provider.storeName}' $t", t)
          }
      }
      _loaded = true
    }
  }

  final
  def stopSupervisorStores(container: FabricWaveSupervisorContainer): Unit = {
    synchronized {
      log info s"STORE_PROVIDER_SUPERVISOR_STOP_ALL"
      _supervisorStoreRegistry.stop
      _loaded = false
    }
  }

  final
  def getSupervisorStore(datasource: FabricDatasource): FabricStoreSupervisor = {
    _supervisorStoreRegistry.getOrThrow(datasource)
  }

  final
  def getSupervisorStore(name: String): FabricStoreSupervisor = {
    _supervisorStoreRegistry.get(name)
  }

}
