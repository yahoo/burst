/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.felt.compile.FeltClassName
import org.burstsys.felt.compile.artifact.{FeltArtifactKey, FeltArtifactTag}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger

/**
 * event dispatch for felt service events
 */
trait FeltTalker extends AnyRef with FeltListener with VitalsLogger {

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _listener: Option[FeltListener] = None

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // LISTENER
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def talksTo(listener: FeltListener): this.type = {
    if (_listener.isDefined)
      throw VitalsException(s"FELT_TALKER_LISTENER_ALREADY_DEFINED")
    _listener = Some(listener)
    this
  }

  final override
  def onFeltSweepClean(key: FeltArtifactKey, tag: FeltArtifactTag): Unit = {
    log info s"FELT_TALKER_ON_FELT_SWEEP_CLEAN(key='$key', tag='$tag')"
    _listener.foreach(_.onFeltSweepClean(key, tag))
  }

  final override
  def onFeltSweepGenerate(key: FeltArtifactKey): Unit = {
    log info s"FELT_TALKER_ON_FELT_SWEEP_GENERATE(key='$key')"
    _listener.foreach(_.onFeltSweepGenerate(key))
  }

  final override
  def onFeltAddToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName, byteCount: Int): Unit = {
    log debug s"FELT_TALKER_ON_FELT_ADD_TO_CLASSLOADER(key='$key', tag='$tag', className='$className')"
    _listener.foreach(_.onFeltAddToClassLoader(key, tag, className, byteCount))
  }

  final override
  def onFeltDeleteFromClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName): Unit =  {
    log info s"FELT_TALKER_ON_FELT_DELETE_FROM_CLASSLOADER(key='$key', tag='$tag', className='$className')"
    _listener.foreach(_.onFeltDeleteFromClassLoader(key, tag, className))
  }
}
