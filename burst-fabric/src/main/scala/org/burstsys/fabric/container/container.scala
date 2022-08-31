/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.{VitalsService, reflection}

import scala.collection.JavaConverters._

package object container extends VitalsLogger {

  type FabricContainerId = Long

  /**
    * All services that are master side
    */
  trait FabricMasterService extends VitalsService {
    def container: FabricMasterContainer
  }

  /**
    * All services that are worker side
    */
  trait FabricWorkerService extends VitalsService {
    def container: FabricWorkerContainer
  }

  final val MasterLog4JPropertiesFileName: String = "master"
  final val WorkerLog4JPropertiesFileName: String = "worker"

  ////////////////////////////////////////////////////////////////////////////////
  // CONTAINER ACCESS
  ////////////////////////////////////////////////////////////////////////////////

  /**
    * find/instantiate the singleton top level fabric master container
    */
  lazy val masterContainer: FabricMasterContainer = {
    val annotationClass = classOf[FabricMasterContainerProvider]
    val classes = reflection.getTypesAnnotatedWith(annotationClass).asScala
    if (classes.size != 1)
      throw VitalsException(s"found ${classes.size} master container(s) instead of a single one!")
    val containerClass = classOf[FabricMasterContainer]
    val clazz = classes.head
    if (!containerClass.isAssignableFrom(clazz))
      throw VitalsException(s"annotation '${annotationClass.getSimpleName}' was not on '${containerClass.getSimpleName}' container class")
    clazz.getDeclaredConstructor().newInstance().asInstanceOf[FabricMasterContainer]
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
