/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.instrument

/**
  * simple elapsed time instrumentation
  */
final case class VitalsElapsedTimer(name: String) {

  private[this] var startNanos: Long = 0L
  private[this] var end: Long = 0L

  def start: this.type = {
    startNanos = System.nanoTime
    this
  }

  def stop: this.type = {
    end = System.nanoTime
    this
  }

  def read: String = {
    prettyTimeFromNanos(System.nanoTime - startNanos)
  }

  def elapsedTime: Long = {
    end - startNanos
  }

  def prettyElapsedTime: String = {
    prettyTimeFromNanos(elapsedTime)
  }

  def currentElapsedTime: Long = {
    System.nanoTime - startNanos
  }
}
