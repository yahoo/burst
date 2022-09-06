/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.store

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._

import scala.jdk.CollectionConverters._
import scala.reflect.{ClassTag, _}

/**
 * central repository for store instances on the worker or master side
 */
trait FabricStoreRegistry[R <: FabricStore] extends VitalsService {

  /**
   * add a (master or worker) store to the local container
   *
   * @param store
   * @return
   */
  def addStore(store: R): FabricStoreRegistry[R]

  /**
   * a list of the current stores
   *
   * @return
   */
  def storeNames: String

  /**
   * get a store by its name
   *
   * @param storeName
   * @return
   */
  def get(storeName: String): R

  /**
   * get the (master or worker) store for the current container
   *
   * @param datasource
   * @return
   */
  def get(datasource: FabricDatasource): R

  /**
   * get the (master or worker) store for the current container, throwing
   * an exception if it is not available.
   *
   * @param datasource
   * @return
   */
  def getOrThrow(datasource: FabricDatasource): R

}

object FabricStoreRegistry {

  def apply[R <: FabricStore : ClassTag](): FabricStoreRegistry[R] = FabricStoreRegistryContext[R]()

}

final case
class FabricStoreRegistryContext[R <: FabricStore : ClassTag]() extends FabricStoreRegistry[R] {

  override val modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"${classTag[R].runtimeClass.getSimpleName}"

  override def toString: String = serviceName

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _storeMap = new ConcurrentHashMap[FabricStoreName, R]

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    log info s"FABRIC_STORE_START storeNames=$storeNames"
    val stores = _storeMap.values.iterator
    while (stores.hasNext) stores.next.startIfNotAlreadyStarted
    this
  }

  override
  def stop: this.type = {
    log info s"FABRIC_STORE_STOP storeNames=$storeNames"
    val stores = _storeMap.values.iterator
    while (stores.hasNext) stores.next.stopIfNotAlreadyStopped
    _storeMap.clear()
    this
  }

  override
  def storeNames: String = _storeMap.asScala.values.map(_.storeName).mkString("'", "', '", "'")

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def get(storeName: String): R = {
    _storeMap.get(storeName) match {
      case null =>
        log error burstStdMsg(s"FAB_STORE_GET storeName='$storeName' FAIL - storeNames=$storeNames")
        null.asInstanceOf[R]
      case s => s
    }
  }

  override
  def get(datasource: FabricDatasource): R = {
    val storeName = datasource.view.storeProperties.getValueOrThrow[String](FabricStoreNameProperty)
    _storeMap.get(storeName) match {
      case null =>
        log error burstStdMsg(s"FAB_STORE_GET datasource=$datasource, storeName='$storeName', FAIL - storeNames=$storeNames")
        null.asInstanceOf[R]
      case s => s
    }
  }

  override
  def getOrThrow(datasource: FabricDatasource): R = {
    val storeName = datasource.view.storeProperties.getValueOrThrow[String](FabricStoreNameProperty)
    _storeMap.get(storeName) match {
      case null => throw VitalsException(s"FAB_STORE_GET datasource=$datasource, storeName='$storeName', FAIL - storeNames=$storeNames")
      case s => s
    }
  }

  override
  def addStore(store: R): FabricStoreRegistry[R] = {
    _storeMap.put(store.storeName, store)
    this
  }
}
