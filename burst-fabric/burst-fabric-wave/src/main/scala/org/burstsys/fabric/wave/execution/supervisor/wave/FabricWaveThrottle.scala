/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.configuration._

/**
 * This throttle counts waves passing through its gate, and limits the number, blocking new ones and waiting
 * for a prior operation to finish before letting the new ones through.
 */
object FabricWaveThrottle {

  @volatile private[this]
  var _currentWaveConcurrency = 0

  private[this]
  val _gate = new Object

  final
  def apply[ResultType <: Any](wave: => ResultType): ResultType = {
    _gate synchronized {
      //noinspection LoopVariableNotUpdated
      while (_currentWaveConcurrency >= burstFabricWaveConcurrencyProperty.getOrThrow) _gate.wait()
      _currentWaveConcurrency += 1
    }
    try {
      FabricWaveReporter.recordConcurrency(_currentWaveConcurrency)
      wave
    } finally {
      _gate synchronized {
        _currentWaveConcurrency -= 1
        _gate.notifyAll()
      }
    }
  }


}
