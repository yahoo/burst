/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model

import org.burstsys.vitals.logging._

package object pipeline extends VitalsLogger {

  /**
   * Pipeline event listeners get the fire hose, they should employ strategies like case matching to
   * listen to the events that they care about.
   */
  trait FabricPipelineEventListener extends PipelineListener[FabricPipelineEvent]

  /**
   * a single event in the Fabric request execution lifecycle pipeline
   */
  trait FabricPipelineEvent extends PipelineEvent

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _pipeline = new FabricEventPipeline[FabricPipelineEvent](s"fab-pipeline-talker")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // public api
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * publish a new event in the execution pipeline
    * @param event the event to publish
    */
  def publishPipelineEvent(event: FabricPipelineEvent): Unit = {
    _pipeline.startIfNotAlreadyStarted
    _pipeline.publish(event)
  }

  /**
    * add a listener for pipeline events
    */
  def addPipelineSubscriber(subscriber: FabricPipelineEventListener): Unit = _pipeline.register(subscriber)

}
