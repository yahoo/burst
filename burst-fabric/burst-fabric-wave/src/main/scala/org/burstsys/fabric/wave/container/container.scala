/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.container.{FabricContainerId, FabricSupervisorContainerProvider, FabricWorkerContainerProvider}
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reflection

import scala.jdk.CollectionConverters._

package object container extends VitalsLogger {

  ////////////////////////////////////////////////////////////////////////////////
  // CONTAINER ACCESS
  ////////////////////////////////////////////////////////////////////////////////

  /**
    * find/instantiate the singleton top level fabric supervisor container
    */
  lazy val supervisorContainer: FabricWaveSupervisorContainer = {
    val annotationClass = classOf[FabricSupervisorContainerProvider]
    val classes = reflection.getTypesAnnotatedWith(annotationClass).asScala
    if (classes.size != 1)
      throw VitalsException(s"found ${classes.size} supervisor container(s) instead of a single one!")
    val containerClass = classOf[FabricWaveSupervisorContainer]
    val clazz = classes.head
    if (!containerClass.isAssignableFrom(clazz))
      throw VitalsException(s"annotation '${annotationClass.getSimpleName}' was not on '${containerClass.getSimpleName}' container class")
    clazz.getDeclaredConstructor().newInstance().asInstanceOf[FabricWaveSupervisorContainer]
  }

  /**
    * find/instantiate the singleton top level fabric worker container
    */
  lazy val workerContainer: FabricWaveWorkerContainer = newWorkerContainerInstance

  def getWorkerContainer(id: FabricContainerId, healthCheckPort: Int): FabricWaveWorkerContainer = {
    val container: FabricWaveWorkerContainer = newWorkerContainerInstance
    container.containerId = id
    container.health.healthCheckPort = healthCheckPort
    container
  }

  private def newWorkerContainerInstance: FabricWaveWorkerContainer = {
    val annotationClass = classOf[FabricWorkerContainerProvider]
    val classes = reflection.getTypesAnnotatedWith(annotationClass).asScala
    if (classes.size != 1)
      throw VitalsException(s"found ${classes.size} worker container(s) instead of a single one!")
    val containerClass = classOf[FabricWaveWorkerContainer]
    val clazz = classes.head
    if (!containerClass.isAssignableFrom(clazz))
      throw VitalsException(s"annotation '${annotationClass.getSimpleName}' was not on '${containerClass.getSimpleName}' container class")
    clazz.getDeclaredConstructor().newInstance().asInstanceOf[FabricWaveWorkerContainer]
  }
}
