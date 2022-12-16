/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.supervisor

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainer
import org.burstsys.fabric.container.FabricContainerContext
import org.burstsys.fabric.net.message.assess.FabricNetAssessRespMsg
import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.server.FabricNetServer
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.message
import org.burstsys.fabric.topology.supervisor.FabricSupervisorTopology
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstStdMsg

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * the one per JVM top level container for a Fabric Supervisor
 */
trait FabricSupervisorContainer[T <: FabricSupervisorListener] extends FabricContainer with FabricNetServerListener {

  /**
   * the supervisor topology service
   */
  def topology: FabricSupervisorTopology

  /**
   * a listener for protocol events
   */
  def talksTo(listener: T*): this.type

}

abstract class FabricSupervisorContainerContext[T <: FabricSupervisorListener](netConfig: FabricNetworkConfig)
  extends FabricContainerContext with FabricSupervisorContainer[T] {

  override def serviceName: String = s"fabric-supervisor-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final lazy
  val bootModality: VitalsServiceModality = if (configuration.burstFabricSupervisorStandaloneProperty.get)
    VitalsStandaloneServer else VitalsStandardServer

  private[this]
  val _net: FabricNetServer = FabricNetServer(this, netConfig)

  private[this]
  val _topology: FabricSupervisorTopology = FabricSupervisorTopology(this)

  protected[this]
  val _listenerSet: mutable.Set[T] = ConcurrentHashMap.newKeySet[T].asScala

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def netServer: FabricNetServer = _net

  override def topology: FabricSupervisorTopology = _topology

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def start: this.type = {
    synchronized {
      ensureNotRunning

      if (containerId.isEmpty) {
        containerId = System.currentTimeMillis()
      }

      // start generic container
      super.start


      // start up the network and topology manager
      _net.start
      _topology.start
      health.registerService(_topology)

      markRunning
    }
    this
  }

  override def stop: this.type = {
    synchronized {
      ensureRunning

      _net.stop
      _topology.stop

      // stop generic container
      super.stop

      markNotRunning
    }
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def talksTo(listeners: T*): this.type = {
    _listenerSet ++= listeners
    this
  }

  override def onNetMessage(connection: FabricNetServerConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Supervisor receieved unknown message mt=$mt")
    }
  }

  override def onDisconnect(connection: FabricNetServerConnection): Unit = {
    _listenerSet.foreach(_.onDisconnect(connection))
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {
    _listenerSet.foreach(_.onNetServerTetherMsg(connection, msg))
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    _listenerSet.foreach(_.onNetServerAssessRespMsg(connection, msg))
  }

  def filteredForeach[I <: FabricSupervisorListener: Manifest](body: I => Unit): Unit = {
    _listenerSet.filter{sl: FabricSupervisorListener =>
      sl match {
        case _: I => true
        case _ => false
      }}.foreach{v => body(v.asInstanceOf[I])}
  }
}

