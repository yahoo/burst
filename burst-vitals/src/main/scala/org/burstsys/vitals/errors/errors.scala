/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.io.{PrintWriter, StringWriter}
import java.nio.file.FileAlreadyExistsException
import java.util.concurrent.TimeoutException

import scala.language.implicitConversions
import scala.util.control.ControlThrowable

package object errors  {

  /**
    * Generalized Burst Error
   * @deprecated
    */
  trait VitalsError {
    def code: Int

    def description: String
  }

  def printStack(t: Throwable): String = {
    val writer = new StringWriter
    val printer = new PrintWriter(writer)
    t.printStackTrace(printer)
    writer.toString
  }

  /**
   * many standard exceptions do a really bad job of printing themselves out consistently.
   * try to do the best we can to normalize...
   * @param t
   * @return
   */
  implicit def messageFromException(t: Throwable): String = {
    val tMsg = t match {
      case toe: TimeoutException =>
        s"${toe.getClass.getSimpleName}:${toe.getMessage}"
      case oob: ArrayIndexOutOfBoundsException =>
        s"${oob.getClass.getSimpleName}:${oob.getMessage}"
      case oob: IndexOutOfBoundsException =>
        s"${oob.getClass.getSimpleName}:${oob.getMessage}"
      case fae: FileAlreadyExistsException =>
        s"${fae.getClass.getSimpleName}:${fae.getFile}"
      case _ =>
        if (t.getMessage == null) t.toString else t.getMessage
    }
    tMsg
  }

  /**
   * Make sure you don't catch a Scala control flow throwable.
   *
   * @param handler
   * @tparam T
   * @return
   */
  def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    case ex: ControlThrowable => throw ex

    case ex: OutOfMemoryError =>
      ex.printStackTrace(System.err)
      // die a sudden death
      sys.exit(-1)

    //If it's an exception they handle, pass it on
    case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)

    // If they didn't handle it, rethrow. This line isn't necessary, just for clarity
    case ex: Throwable => throw ex
  }

}
