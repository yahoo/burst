/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.lang.management.{ManagementFactory, ThreadInfo}

import org.burstsys.vitals.errors.messageFromException

import scala.language.implicitConversions

package object logging extends VitalsLogger {

  final case class BurstModuleName(value: String) extends AnyVal {
    override def toString: String = value
  }

  implicit def stringToModuleName(s: String): BurstModuleName = BurstModuleName(s)

  implicit def moduleToString(m: BurstModuleName): String = m.value

  private[logging]
  def bridgeJul: this.type = {
    System.setProperty("java.util.logging.manager", classOf[org.apache.logging.log4j.jul.LogManager].getName)
    this
  }

  final val MAX_FRAMES: Int = 100

  final
  def burstStdMsg(msg: String)(implicit burstModuleName: BurstModuleName): String = {
    printMsg(msg, burstModuleName, getFirstBurstTrace(Thread.currentThread.getStackTrace))
  }

  private
  def printMsg(msg: String, burstModuleName: BurstModuleName, trace: Option[StackTraceElement]) = {
    trace match {
      case None => s"$burstModuleName '$msg' unknown source location on $burstHost"
      case Some(location) =>
        val className = location.getClassName match {
          case null => "(unknown class)"
          case name => name.split('.').last.stripSuffix("$class").stripSuffix("$").replaceAll("\\$\\$anonfun\\$", ".")
        }
        val fileName = location.getFileName match {
          case null => "(unknown file)"
          case name => name
        }
        s"$burstModuleName $msg at $fileName:${location.getLineNumber} on $burstHost"
    }
  }

  final
  def burstStdMsg(t: Throwable)(implicit burstModuleName: BurstModuleName): String = {
    printMsg(messageFromException(t), burstModuleName, getFirstBurstTrace(t.getStackTrace))
  }

  final
  def burstStdMsg(msg: String, t: Throwable)(implicit burstModuleName: BurstModuleName): String = {
    printMsg(s"$msg: ${t: String}", burstModuleName, getFirstBurstTrace(t.getStackTrace))
  }

  final
  def getFirstBurstTrace(stack: Array[StackTraceElement]): Option[StackTraceElement] = {
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

  final
  def getAllThreadsDump: Array[String] = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    for (ti <- threadMxBean.dumpAllThreads(true, true)) yield verboseThreadToString(ti)
  }

  final
  def getPrunedThreadsDump(f: ThreadInfo => Option[ThreadInfo]): Array[ThreadInfo] = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    (for (ti <- threadMxBean.dumpAllThreads(true, true)) yield f(ti)).flatten
  }


  final
  def getThisThreadDump: String = {
    val threadMxBean = ManagementFactory.getThreadMXBean
    verboseThreadToString(threadMxBean.getThreadInfo(Thread.currentThread().getId, MAX_FRAMES))
  }

  final
  def verboseThreadToString(thread: ThreadInfo): String = {
    val sb = new StringBuilder("\"" + thread.getThreadName + "\"" + " Id=" + thread.getThreadId + " " + thread.getThreadState)
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

      {
        i += 1;
        i - 1
      }
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
