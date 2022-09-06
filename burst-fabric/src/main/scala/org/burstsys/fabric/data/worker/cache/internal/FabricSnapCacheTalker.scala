/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache.internal

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.data.worker.cache.{FabricSnapCache, FabricSnapCacheListener}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * [[FabricSnapCacheListener]] collection and message distribution in [[FabricSnapCache]]
 */
trait FabricSnapCacheTalker extends AnyRef with FabricSnapCache {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  val _listeners = ConcurrentHashMap.newKeySet[FabricSnapCacheListener].asScala

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  def talk[U](f: FabricSnapCacheListener => U): Unit = _listeners.toSet.foreach(f)

  final protected
  def clearListeners(): Unit = _listeners.clear()

  final override
  def talksTo(listeners: FabricSnapCacheListener*): FabricSnapCache = {
    _listeners ++= listeners
    this
  }

}
