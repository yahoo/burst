/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import com.sun.management.OperatingSystemMXBean
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument.prettyTimeFromMillis

import java.lang.management.ManagementFactory._
import java.lang.management.{BufferPoolMXBean, ManagementFactory}
import java.net.InetAddress
import scala.jdk.CollectionConverters._

package object host extends VitalsLogger {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // JAVA Heap Memory
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the amount of used heap memory in bytes.
   *
   * @return the amount of used heap memory in bytes.
   *
   */
  def heapUsed: Long = getMemoryMXBean.getHeapMemoryUsage.getUsed

  /**
   * Returns the amount of heap memory in bytes that is committed for
   * the Java virtual machine to use.  This amount of heap memory is
   * guaranteed for the Java virtual machine to use.
   *
   * @return the amount of committed memory in bytes.
   *
   */
  def heapCommitted: Long = getMemoryMXBean.getHeapMemoryUsage.getCommitted

  /**
   * Returns the maximum amount of heap memory in bytes that can be
   * used for memory management.  This method returns <tt>-1</tt>
   * if the maximum memory size is undefined.
   *
   * <p> This amount of heap memory is not guaranteed to be available
   * for heap memory management if it is greater than the amount of
   * committed memory.  The Java virtual machine may fail to allocate
   * heap memory even if the amount of used heap memory does not exceed this
   * maximum size.
   *
   * @return the maximum amount of heap memory in bytes;
   *         <tt>-1</tt> if undefined.
   */
  def heapMax: Long = getMemoryMXBean.getHeapMemoryUsage.getMax

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Native/Direct/OffHeap Memory
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def mappedMemoryPoolBean = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName == "mapped")

  def mappedMemoryUsed: Long = mappedMemoryPoolBean.map(_.getMemoryUsed).getOrElse(-1)


  private def directMemoryPoolBean = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName == "direct")

  def directMemoryUsed: Long = directMemoryPoolBean.map(_.getMemoryUsed).getOrElse(-1)


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // OS (all processes) Memory
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the total amount of physical memory in bytes.
   *
   * @return
   */
  def osTotalPhysMemory: Long = getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getTotalPhysicalMemorySize


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the number of processors available to the Java virtual machine.
   * This method is equivalent to the {@link Runtime#availableProcessors()}
   * method.
   * <p> This value may change during a particular invocation of
   * the virtual machine.
   *
   * @return the number of processors available to the virtual
   *         machine; never smaller than one.
   */
  def osTotalCores: Int = getOperatingSystemMXBean.getAvailableProcessors

  /**
   * Returns the current number of live threads including both
   * daemon and non-daemon threads.
   *
   * @return the current number of live threads.
   */

  def threadsCurrent: Int = getThreadMXBean.getThreadCount

  /**
   * Returns the CPU user mode in nanoseconds for all threads
   * The returned value is of nanoseconds precision but
   * not necessarily nanoseconds accuracy.
   */
  def threadsUserCpuTime: Long = (for (t <- getThreadMXBean.getAllThreadIds.indices) yield {
    val time = getThreadMXBean.getThreadUserTime(t + 1)
    if (time == -1) 0 else time
  }).sum

  /**
   * Returns the total CPU time for all thread in nanoseconds.
   * The returned value is of nanoseconds precision but
   * not necessarily nanoseconds accuracy.
   * If the implementation distinguishes between user mode time and system
   * mode time, the returned CPU time is the amount of time that
   * the thread has executed in user mode or system mode.
   */
  def threadsTotalCpuTime: Long = (for (t <- getThreadMXBean.getAllThreadIds.indices) yield {
    val time = getThreadMXBean.getThreadCpuTime(t + 1)
    if (time == -1) 0 else time
  }).sum

  /**
   * Returns the peak live thread count since the Java virtual machine
   * started or peak was reset.
   */
  def threadsPeak: Int = getThreadMXBean.getPeakThreadCount

  /**
   * Returns the system load average for the last minute.
   * The system load average is the sum of the number of runnable entities
   * queued to the {@linkplain #getAvailableProcessors available processors}
   * and the number of runnable entities running on the available processors
   * averaged over a period of time.
   * The way in which the load average is calculated is operating system
   * specific but is typically a damped time-dependent average.
   * <p>
   * If the load average is not available, a negative value is returned.
   * <p>
   * This method is designed to provide a hint about the system load
   * and may be queried frequently.
   * The load average may be unavailable on some platform where it is
   * expensive to implement this method.
   *
   * @return the system load average; or a negative value if not available.
   */
  def loadAverage: Double = getOperatingSystemMXBean.getSystemLoadAverage

  val cores: String = getOperatingSystemMXBean.getAvailableProcessors.toString

  val hostName: String = InetAddress.getLocalHost.getHostName

  private def garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans.asScala.toArray


  def gcReadout: String = (for (b <- garbageCollectorMXBeans)
    yield s"${b.getCollectionCount}/${prettyTimeFromMillis(b.getCollectionTime)}").mkString(", ")

  /**
   * Returns the uptime of the Java virtual machine in milliseconds.
   *
   * @return uptime of the Java virtual machine in milliseconds.
   */
  def uptime: Long = getRuntimeMXBean.getUptime

  val osName: String = getOperatingSystemMXBean.getName
  val osVersion: String = getOperatingSystemMXBean.getVersion
  val osArch: String = getOperatingSystemMXBean.getArch
}
