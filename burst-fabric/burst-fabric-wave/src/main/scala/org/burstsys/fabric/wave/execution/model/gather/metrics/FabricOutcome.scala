/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.metrics

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.execution.model.result.state._
import org.burstsys.fabric.wave.execution.model.result.status._
import org.burstsys.vitals.errors
import org.burstsys.vitals.errors.{VitalsException, messageFromException, printStack}
import org.burstsys.vitals.logging.burstStdMsg

/**
 * tracking of outcomes during a scan
 */
trait FabricOutcome extends AnyRef {

  def clearCollector(): Unit = {}

  def clearDictionary(): Unit = {}

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  var _scanState: FabricScanState = FabricScanRunning

  @transient private[this]
  var _hadException: Boolean = false

  @transient private[this]
  var _exception: Throwable = _

  @transient private[this]
  var _exceptionStack: String = _

  @transient private[this]
  var _messages = new Array[String](10)

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def resultStatus: FabricResultStatus = {
    _scanState match {
      case FabricScanRunning => FabricSuccessResultStatus
      case FabricSuccessStatus => FabricSuccessResultStatus
      case FabricExceptionStatus => FabricFaultResultStatus
      case FabricInvalidStatus => FabricInvalidResultStatus
      case FabricTimeoutStatus => FabricTimeoutResultStatus
      case FabricNotReadyStatus => FabricNotReadyResultStatus
      case FabricStoreErrorStatus => FabricFaultResultStatus
      case FabricViewErrorStatus => FabricFaultResultStatus
      case FabricNoDataStatus => FabricNoDataResultStatus
      case _ => ???
    }
  }

  final
  def scanState(state: FabricScanState): Unit = _scanState = state

  final
  def scanState: FabricScanState = _scanState

  final
  def exception: Throwable = _exception

  final
  def exceptionStack: String = if (_hadException) _exceptionStack else "NONE"

  final
  def hadException: Boolean = _hadException

  final
  def messages: Array[String] = _messages

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // toString
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def outcomeAsString: String = {
    val filteredMessages = messages.filter(_ != null)
    val msgs = if (filteredMessages.nonEmpty) s", ${filteredMessages.mkString}" else ""
    val es = if (exceptionStack == "NONE") "" else s", $exceptionStack"
    s"$scanState $es $msgs"
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // merge
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def mergeOutcome(thatOutcome: FabricOutcome): Unit = {
    if (thatOutcome.hadException) {
      this._exceptionStack = thatOutcome.exceptionStack
      this._hadException = true
      this._messages = this._messages.filter(_ != null) ++ thatOutcome.messages
      this._scanState = thatOutcome.scanState
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def initializeOutcome(): Unit = {
    initMessages()
    _hadException = false
    _exception = null
    _scanState = FabricScanRunning
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Exceptions
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * if something was thrown - mark that here so we can return it
   * to the supervisor
   *
   * @param t
   * @return
   */
  final
  def markException(t: Throwable): this.type = {
    val tag = s"FabricOutcome.markException"
    _scanState = FabricExceptionStatus
    _hadException = true

    _exception = if (t == null) {
      val msg = burstStdMsg(s"$tag given null throwable ")
      log error msg
      new RuntimeException(msg).fillInStackTrace()
    } else t

    // capture the stack trace as a string to return
    _exceptionStack = printStack(_exception)

    // log this locally
    log error burstStdMsg(s"$tag FAIL ${messageFromException(_exception)}\n****\n${_exceptionStack}\n****")

    this
  }

  final
  def thisOutcomeOrThatOutcomeInvalid(thatOutcome: FabricOutcome): Boolean = {
    thatOutcome.scanState match {
      case FabricInvalidStatus =>
        clearCollector()
        appendStatusMessage(thatOutcome.messages)
        this.scanState(FabricInvalidStatus)
        return true
      case _ =>
    }

    this.scanState match {
      case FabricInvalidStatus =>
        clearCollector()
        return true
      case _ =>
    }

    // if we notice that the query is being told to gracefully stop..
    if (thatOutcome.scanState.damped) {
      this.scanState(thatOutcome.scanState)
      return true
    }
    false
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Messages
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Attempt to add this messages to a short list of non dup messages.
   * Reject this messages tacitly if its a dup or a preexisting message
   * or if we already have filled the message list
   *
   * @param m1
   * @return
   */
  @inline final
  def appendStatusMessage(m1: String): Array[String] = {
    var i = 0
    var dup = false
    while (i < messages.length && !dup) {
      val m = messages(i)
      if (m != null && m.equals(m1)) dup = true
      i += 1
    }
    if (dup) return messages
    i = 0
    var done = false
    while (i < messages.length && !done) {
      val m = messages(i)
      if (m == null) {
        messages(i) = m1
        done = true
      }
      i += 1
    }
    messages
  }

  @inline final
  def appendStatusMessage(msgs: Array[String]): Array[String] = {
    msgs foreach appendStatusMessage
    messages
  }

  private
  def initMessages(): Unit = {
    var i = 0
    while (i < _messages.length) {
      _messages(i) = null
      i += 1
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * serialize on worker to send back to supervisor
   *
   * @param k
   * @param out
   */
  final
  def writeOutcome(k: Kryo, out: Output): Unit = {
    try {
      k.writeClassAndObject(out, _scanState)
      out writeBoolean _hadException
      if (_hadException) {
        out writeString _exceptionStack
      }
      out writeInt _messages.length
      var i = 0
      while (i < _messages.length) {
        out writeString _messages(i)
        i += 1
      }
    } catch errors.safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  /**
   * deserialize on supervisor after receipt from worker
   *
   * @param k
   * @param in
   */
  final
  def readOutcome(k: Kryo, in: Input): Unit = {
    try {
      _scanState = k.readClassAndObject(in).asInstanceOf[FabricScanState]
      _hadException = in.readBoolean
      if (_hadException) {
        _exceptionStack = in.readString
      }
      _messages = new Array[String](in.readInt)
      var i = 0
      while (i < _messages.length) {
        _messages(i) = in.readString()
        i += 1
      }
    } catch errors.safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }


}
