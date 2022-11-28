/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.errors.messageFromException
import sourcecode.Enclosing
import sourcecode.FileName
import sourcecode.Line
import sourcecode.Pkg

import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import scala.annotation.unused
import scala.collection.mutable
import scala.language.implicitConversions

package object logging extends VitalsLogger {

  def packageToModuleName(pkg: Pkg): String = s"BURST${pkg.value.stripPrefix(burstPackage).toUpperCase.replaceAll("\\.", "_")}"

  private[logging] def bridgeJul: this.type = {
    System.setProperty("java.util.logging.manager", classOf[org.apache.logging.log4j.jul.LogManager].getName)
    this
  }

  final val MAX_FRAMES: Int = 100

  /**
   * Format a message including a package prefix and file/line number
   *
   * @param msg the message to format
   * @return the formatted message
   */
  final def burstStdMsg(msg: String)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    formatMsg(msg, located = false)
  }

  /**
   * Format a message including a package prefix, file/line number, and enclosing call site
   *
   * @param msg the message to format
   * @return the formatted message
   */
  final def burstLocMsg(msg: String)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    formatMsg(msg, located = true)
  }

  /**
   * Extract a message from an exception including a package prefix and file/line number and enclosing call.
   *
   * @param t the throwable to extract a mesasge from
   * @return the formatted message
   */
  final def burstStdMsg(t: Throwable)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    burstLocMsg(t)
  }

  /**
   * Extract a message from an exception including a package prefix, file/line number, and enclosing call site
   *
   * @param t the throwable to extract a mesasge from
   * @return the formatted message
   */
  final def burstLocMsg(t: Throwable)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    formatMsg(errors.messageFromException(t), located = true)
  }

  /**
   * Extract a message from an exception including a package prefix and file/line number
   *
   * @param msg the message to format
   * @param t   the throwable to extract a mesasge from
   * @return the formatted message
   */
  final def burstStdMsg(msg: String, t: Throwable)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    formatMsg(s"$msg: ${errors.messageFromException(t): String}", located = false)
  }

  /**
   * Extract a message from an exception including a package prefix, file/line number, and enclosing call site
   *
   * @param msg the message to format
   * @param t   the throwable to extract a mesasge from
   * @return the formatted message
   */
  final def burstLocMsg(msg: String, t: Throwable)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    formatMsg(s"$msg: ${errors.messageFromException(t): String}", located = true)
  }

  private def formatMsg(msg: String, located: Boolean)(implicit site: Enclosing, pkg: Pkg, file: FileName, line: Line): String = {
    val location = if (located) s" ${enclosingToLocation(site)}" else ""
    s"${packageToModuleName(pkg)}:$location $msg at ${file.value}:${line.value}"
  }

  private def enclosingToLocation(site: Enclosing): String = {
    val loc = site.value
    val trimmed = if (loc.contains("package")) {
      val components = loc.split("""\.""")
      components.slice(components.indexOf("package") - 1, components.size).mkString(".")
    } else {
      loc.substring(loc.lastIndexOf(".") + 1)
    }
    trimmed.replace("#", ".")
  }

  final def getFirstBurstTrace(stack: Array[StackTraceElement]): Option[StackTraceElement] = {
    if (stack.isEmpty) return None
    stack foreach {
      s =>
        if (
          !s.getClassName.contains("VitalsLogger")
            && !s.getClassName.contains("VitalsError")
            && !s.getClassName.contains("VitalsException")
            && !s.getMethodName.startsWith("burstStdMsg")
            && !s.getMethodName.startsWith("apply")
            && s.getClassName.contains(burstPackage)
        )
          return Some(s)
    }
    stack foreach {
      s =>
        if (
          !s.getClassName.contains("VitalsLogger")
            && !s.getClassName.contains("VitalsException")
            && !s.getMethodName.startsWith("burstStdMsg")
            && s.getClassName.contains(burstPackage)
        )
          return Some(s)
    }
    Some(stack.head)
  }

  @unused
  final
  def getAllThreadsDump: Array[String] = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    for (ti <- threadMxBean.dumpAllThreads(true, true)) yield verboseThreadToString(ti)
  }

  @unused
  final
  def getPrunedThreadsDump(f: ThreadInfo => Option[ThreadInfo]): Array[ThreadInfo] = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    (for (ti <- threadMxBean.dumpAllThreads(true, true)) yield f(ti)).flatten
  }


  @unused
  final
  def getThisThreadDump: String = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    verboseThreadToString(threadMxBean.getThreadInfo(Thread.currentThread().getId, MAX_FRAMES))
  }

  final
  def verboseThreadToString(thread: ThreadInfo): String = {
    val sb = new mutable.StringBuilder("\"" + thread.getThreadName + "\"" + " Id=" + thread.getThreadId + " " + thread.getThreadState)
    if (thread.getLockName != null) sb.append(" on " + thread.getLockName)
    if (thread.getLockOwnerName != null) sb.append(" owned by \"" + thread.getLockOwnerName + "\" Id=" + thread.getLockOwnerId)
    if (thread.isSuspended) sb.append(" (suspended)")
    if (thread.isInNative) sb.append(" (in native)")
    sb.append(" \n")
    var i = 0

    val stackTrace = thread.getStackTrace
    while (i < stackTrace.length && i < MAX_FRAMES) {
      val ste = stackTrace(i)
      sb.append("\t at " + ste.toString)
      sb.append(" \n")
      if (i == 0 && thread.getLockInfo != null) {
        val ts = thread.getThreadState
        ts match {
          case Thread.State.BLOCKED =>
            sb.append("\t -  blocked on " + thread.getLockInfo)
            sb.append(" \n")
          case Thread.State.WAITING =>
            sb.append("\t -  waiting on " + thread.getLockInfo)
            sb.append(" \n")
          case Thread.State.TIMED_WAITING =>
            sb.append("\t -  waiting on " + thread.getLockInfo)
            sb.append(" \n")
          case _ =>
        }
      }
      val lockedMonitors = thread.getLockedMonitors
      for (mi <- lockedMonitors) {
        if (mi.getLockedStackDepth == i) {
          sb.append("\t -  locked " + mi)
          sb.append(" \n")
        }
      }

      i += 1
    }
    if (i < stackTrace.length) {
      sb.append("\t...")
      sb.append(" \n")
    }
    val locks = thread.getLockedSynchronizers
    if (locks.nonEmpty) {
      sb.append(" \n\t Number of locked synchronizers = " + locks.length)
      sb.append(" \n")
      for (li <- locks) {
        sb.append(" \t - " + li)
        sb.append(" \n")
      }
    }
    sb.append(" \n")
    sb.toString
  }


}
