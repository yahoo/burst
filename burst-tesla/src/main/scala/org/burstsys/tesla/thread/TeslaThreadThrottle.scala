/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.thread

import java.util.concurrent.ArrayBlockingQueue

/**
 * This throttle counts threads passing through its gate, and limits the number, blocking new ones and waiting
 * for a prior operation to finish before letting the new ones through. This is a fair scheduler with FIFO semantics.
 */
final case
class TeslaThreadThrottle(name: String, maxConcurrency: Int) {

  @volatile private[this]
  var _activeThreads = 0

  @volatile private[this]
  var _waitingThreads = 0

  private[this]
  val _waitQueue = new ArrayBlockingQueue[Thread](1000)

  def apply[ResultType <: Any](body: => ResultType): ResultType = {
    val thisThread = Thread.currentThread()
    thisThread synchronized {
      //noinspection LoopVariableNotUpdated
      if (_activeThreads >= maxConcurrency) {
        log info s"${Thread.currentThread.getName} ENTER"
        _waitingThreads += 1
        _waitQueue.put(thisThread)
        thisThread.wait()
        log info s"${Thread.currentThread.getName} EXIT"
      }
      _activeThreads += 1
    }
    try {
      body
    } finally {
      _activeThreads -= 1
      if (_waitingThreads > 0) {
        val nextThread = _waitQueue.poll()
        log info s"${nextThread.getName} RELEASE"
        nextThread synchronized {
          _waitingThreads -= 1
          nextThread.notify()
        }
      }
    }
  }


}
