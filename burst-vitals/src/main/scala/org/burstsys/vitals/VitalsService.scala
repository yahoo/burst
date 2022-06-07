/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.util.Date

import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.VitalsException

/**
  * base class for all Vitals services
  */
trait VitalsService extends AnyRef {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  def modality: VitalsServiceModality

  def startingMessage: String = s"VITALS_${modality}_STARTING '$serviceName'"

  def startedWithDateMessage: String = s"VITALS_${modality}_STARTED '$serviceName' at '${new Date}'"

  def startedMessage: String = s"VITALS_${modality}_STARTED '$serviceName'"

  def stoppingMessage: String = s"VITALS_${modality}_STOPPING '$serviceName'"

  def stoppedMessage: String = s"VITALS_${modality}_STOPPED '$serviceName'"

  def stoppedWithDateMessage: String = s"VITALS_${modality}_STOPPED '$serviceName'at '${new Date}'"

  override def toString: String = serviceName

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal state
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  private[this]
  var _running: Boolean = false

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def ensureRunning: this.type = {
    if (!_running)
      throw VitalsException(s"VITALS_'$serviceName' not running!!")
    this
  }

  final
  def startIfNotRunning: this.type = {
    synchronized {
      if (!_running) {
        start
        markRunning
      }
    }
    this
  }

  final
  def ensureNotRunning: this.type = {
    if (_running)
      throw VitalsException(s"VITALS_'$serviceName' is running!!")
    this
  }

  final
  def markRunning: this.type = {
    _running = true
    this
  }

  final
  def markNotRunning: this.type = {
    _running = false
    this
  }

  final
  def isRunning: Boolean = _running

  def serviceName: String = this.getClass.getSimpleName.stripPrefix("Burst").stripSuffix("$").stripSuffix("Context").stripSuffix("Provider")

  def start: this.type

  final
  def startIfNotAlreadyStarted: this.type = {
    synchronized {
      if (!_running)
        start
    }
    this
  }

  final
  def stopIfNotAlreadyStopped: this.type = {
    synchronized {
      if (_running)
        stop
    }
    this
  }

  def stop: this.type

}

object VitalsService {

  /**
    * The nature of this service in terms of lifetime, cardinality, and server/client roles
    *
    * @param isServer
    * @param isStandalone
    */
  sealed case class VitalsServiceModality(isServer: Boolean, isStandalone: Boolean, isContainer:Boolean = false) {
    def isClient: Boolean = !isServer

    override def toString: String = getClass.getSimpleName.stripPrefix("Vitals").stripSuffix("$").toUpperCase
  }

  /**
    * A unlimited instance per JVM with a __client__ role
    **/
  object VitalsStandardClient extends VitalsServiceModality(isServer = false, isStandalone = false)

  /**
    * A unlimited instance per JVM with a __server__ role
    **/
  object VitalsStandardServer extends VitalsServiceModality(isServer = true, isStandalone = false)

  /**
    * * A unlimited instance per JVM with a __standalone__ server role
    */
  object VitalsStandaloneServer extends VitalsServiceModality(isServer = true, isStandalone = true)

  /**
    * A single per JVM service
    */
  object VitalsSingleton extends VitalsServiceModality(isServer = false, isStandalone = true)

  /**
    * A unlimited instance per JVM without a client or server role
    */
  object VitalsPojo extends VitalsServiceModality(isServer = false, isStandalone = true)

  /**
    * A single top level service per JVM
    */
  object VitalsContainer extends VitalsServiceModality(isServer = false, isStandalone = false, isContainer = true)

}
