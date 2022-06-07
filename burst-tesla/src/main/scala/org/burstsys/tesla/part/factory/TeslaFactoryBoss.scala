/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part.factory

import org.burstsys.tesla.part
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.VitalsService.VitalsSingleton
import org.burstsys.vitals.errors.VitalsException

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
 * periodically print out one report for all parts which includes:
 * - name of part
 * - total bytes
 * - total count
 */
object TeslaFactoryBoss extends VitalsService {

  override val modality: VitalsServiceModality = VitalsSingleton

  override val serviceName: String = s"tesla-factory-boss"

  private
  val _factoryMap = new ConcurrentHashMap[String, TeslaPartFactory[_, _]]

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * free all ''inuse'' parts across all factors and pools
   *
   * @return
   */
  final
  def freeAllUnusedParts: Long = _factoryMap.values.stream().mapToLong(_.freeAllUnusedParts).sum

  /**
   * call in unit tests to be sure that test ended with no parts ''inuse''
   * throw [[VitalsException]] if there are any ''inuse'' parts across all factors and pools
   *
   * @return
   */
  final
  def assertNoInUseParts(): Unit = {
    part.maxPartsFreedInOneRun = Int.MaxValue
    freeAllUnusedParts // first make sure all unused blocks are released
    val msgs = (_factoryMap.values.asScala map {
      factory => if (factory.inUseParts > 0) s"(partName=${factory.partName}, inUseParts=${factory.inUseParts})" else null
    }).filter(_ != null).toArray
    if (msgs.nonEmpty)
      throw VitalsException(s"IN_USE_PARTS! $serviceName ${msgs.mkString("\n\t", ",\n\t", "\n")}")
    else log info s"NO_IN_USE_PARTS $serviceName"
  }

  /**
   * register your factory here
   *
   * @param factory
   * @return
   */
  final
  def registerFactory(factory: TeslaPartFactory[_, _]): this.type = {
    _factoryMap put(factory.partName, factory)
    log debug s"$serviceName registerFactory(factory=${factory.partName})"
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    //    _watcher.startIfNotAlreadyStarted
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    //    _watcher.stop
    markNotRunning
    this
  }

}
