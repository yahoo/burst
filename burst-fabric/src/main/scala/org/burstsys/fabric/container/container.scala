/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.{VitalsService, reflection}

import scala.jdk.CollectionConverters._

package object container extends VitalsLogger {

  type FabricContainerId = Long

  /**
    * All services that are supervisor side
    */
  trait FabricSupervisorService extends VitalsService {
    def container: FabricSupervisorContainer
  }

  /**
    * All services that are worker side
    */
  trait FabricWorkerService extends VitalsService {
    def container: FabricWorkerContainer
  }

  final val SupervisorLog4JPropertiesFileName: String = "supervisor"
  final val WorkerLog4JPropertiesFileName: String = "worker"

  ////////////////////////////////////////////////////////////////////////////////
  // CONTAINER ACCESS
  ////////////////////////////////////////////////////////////////////////////////

  /**
    * find/instantiate the singleton top level fabric supervisor container
    */
  lazy val supervisorContainer: FabricSupervisorContainer = {
    val annotationClass = classOf[FabricSupervisorContainerProvider]
    val classes = reflection.getTypesAnnotatedWith(annotationClass).asScala
    if (classes.size != 1)
      throw VitalsException(s"found ${classes.size} supervisor container(s) instead of a single one!")
    val containerClass = classOf[FabricSupervisorContainer]
    val clazz = classes.head
    if (!containerClass.isAssignableFrom(clazz))
      throw VitalsException(s"annotation '${annotationClass.getSimpleName}' was not on '${containerClass.getSimpleName}' container class")
    clazz.getDeclaredConstructor().newInstance().asInstanceOf[FabricSupervisorContainer]
  }

  /**
    * find/instantiate the singleton top level fabric worker container
    */
  lazy val workerContainer: FabricWorkerContainer = newWorkerContainerInstance

  def getWorkerContainer(id: Long, healthCheckPort: Int): FabricWorkerContainer = {
    val container: FabricWorkerContainer = newWorkerContainerInstance
    container.containerId = id
    container.health.healthCheckPort = healthCheckPort
    container
  }

  private def newWorkerContainerInstance: FabricWorkerContainer = {
    val annotationClass = classOf[FabricWorkerContainerProvider]
    val classes = reflection.getTypesAnnotatedWith(annotationClass).asScala
    if (classes.size != 1)
      throw VitalsException(s"found ${classes.size} worker container(s) instead of a single one!")
    val containerClass = classOf[FabricWorkerContainer]
    val clazz = classes.head
    if (!containerClass.isAssignableFrom(clazz))
      throw VitalsException(s"annotation '${annotationClass.getSimpleName}' was not on '${containerClass.getSimpleName}' container class")
    clazz.getDeclaredConstructor().newInstance().asInstanceOf[FabricWorkerContainer]
  }
}
