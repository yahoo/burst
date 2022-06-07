/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile

import org.burstsys.vitals.errors.{VitalsException, _}

import scala.collection.mutable
import scala.reflect.internal.util.Position
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.AbstractReporter

/**
 * Error reporting for the in memory scala compiler implementation
 *
 * @param settings
 * @param lineOffset
 */
final case
class FeltCompileReporter(settings: Settings, lineOffset: Int) extends AbstractReporter {

  private[this]
  val _messages = new mutable.ListBuffer[List[String]]

  def display(pos: Position, message: String, severity: Severity): Unit = {
    _messages synchronized {
      severity.count += 1
      val severityName = severity match {
        case ERROR =>
          " error: "
        case WARNING =>
          " warning: "
        case INFO =>
          " warning: "
        case _ => ""
      }
      // the line number is not always available
      val lineMessage =
        try {
          "line " + (pos.line - lineOffset)
        } catch safely {
          case _: Exception => ""
        }
      val text = (severityName + lineMessage + ": " + message) ::
        (if (pos.isDefined) {
          pos.finalPosition.lineContent.stripLineEnd ::
            (" " * (pos.column - 1) + "^") ::
            Nil
        } else {
          Nil
        })
      severity match {
        case ERROR => _messages += text
        case WARNING => log debug text
        case INFO => log debug text
        case _ => log error text
      }
    }
  }

  def throwIfError(): Unit = {
    _messages synchronized {
      if (hasErrors /*|| WARNING.count > 0*/ ) {
        val str = _messages.toList.map(_.mkString("\n")).mkString("\n", "\n", "\n")
        throw VitalsException(str)
      }
    }
  }

  def displayPrompt(): Unit = {
    // no.
  }

  override def reset(): Unit = {
    _messages synchronized {
      super.reset()
      _messages.clear()
    }
  }
}
