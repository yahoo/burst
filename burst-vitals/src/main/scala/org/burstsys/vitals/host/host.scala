/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import com.sun.management.{HotSpotDiagnosticMXBean, OperatingSystemMXBean, UnixOperatingSystemMXBean}
import org.burstsys.vitals.instrument.prettyTimeFromMillis
import org.burstsys.vitals.logging._
import oshi.SystemInfo

import java.lang.management.ManagementFactory._
import java.lang.management.{BufferPoolMXBean, GarbageCollectorMXBean, ManagementFactory}
import java.net.InetAddress
import scala.collection.JavaConverters._

package object host extends VitalsLogger {

  def openFiles: Long = getOperatingSystemMXBean match {
    case osBean: UnixOperatingSystemMXBean => osBean.getOpenFileDescriptorCount
    case _ => ???
  }

  def maxFiles: Long = getOperatingSystemMXBean match {
    case osBean: UnixOperatingSystemMXBean => osBean.getMaxFileDescriptorCount
    case _ => ???
  }

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

  private def directMemoryPoolBean = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName == "direct")

  def directMemoryUsed: Long = directMemoryPoolBean.map(_.getMemoryUsed).getOrElse(-1)

  def directMemoryMax: Long = directMemoryPoolBean.map(_.getTotalCapacity).getOrElse(-1)

  private def mappedMemoryPoolBean = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName == "mapped")

  def mappedMemoryUsed: Long = mappedMemoryPoolBean.map(_.getMemoryUsed).getOrElse(-1)

  def mappedMemoryMax: Long = mappedMemoryPoolBean.map(_.getTotalCapacity).getOrElse(-1)

  /**
   * Returns the amount of used non heap memory in bytes.
   *
   * @return the amount of used non heap memory in bytes.
   *
   */
  def nonHeapUsed: Long = getMemoryMXBean.getNonHeapMemoryUsage.getUsed

  /**
   * Returns the amount of non heap memory in bytes that is committed for
   * the Java virtual machine to use.  This amount of non heap memory is
   * guaranteed for the Java virtual machine to use.
   *
   * @return the amount of committed non heap memory in bytes.
   *
   */
  def nonHeapCommitted: Long = getMemoryMXBean.getNonHeapMemoryUsage.getCommitted

  /**
   * Returns the maximum amount of non heap memory in bytes that can be
   * used for non heap memory management.  This method returns <tt>-1</tt>
   * if the maximum non heap memory size is undefined.
   *
   * <p> This amount of non heap memory is not guaranteed to be available
   * for non heap memory management if it is greater than the amount of
   * committed non heap memory.  The Java virtual machine may fail to allocate
   * non heap memory even if the amount of used non heap memory does not exceed this
   * maximum size.
   *
   * @return the maximum amount of non heap memory in bytes;
   *         <tt>-1</tt> if undefined.
   */
  def nonHeapMax: Long = getMemoryMXBean.getNonHeapMemoryUsage.getMax

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // OS (all processes) Memory
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the total amount of physical memory in bytes.
   *
   * @return
   */
  def osTotalPhysMemory: Long = getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getTotalPhysicalMemorySize

  /**
   * Returns the amount of free physical memory in bytes
   *
   * @return
   */
  def osFreePhysMemory: Long = getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getFreePhysicalMemorySize

  /**
   * Returns the amount of virtual memory that is guaranteed to be available to the running process in bytes,
   * or -1 if this operation is not supported.
   *
   * @return
   */
  def osCommittedVirtualMemory: Long = getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getCommittedVirtualMemorySize

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

  /**
   * Returns the total number of collections that have occurred.
   * This method returns <tt>-1</tt> if the collection count is undefined for
   * this collector.
   *
   * @return the total number of collections that have occurred.
   */
  def gcCollectionCount: Long = garbageCollectorMXBeans.map { b: GarbageCollectorMXBean =>
    if (b.isValid)
      b.getCollectionCount
    else
      0
  }.sum

  /**
   * Returns the approximate accumulated collection elapsed time
   * in milliseconds.  This method returns <tt>-1</tt> if the collection
   * elapsed time is undefined for this collector.
   * <p>
   * The Java virtual machine implementation may use a high resolution
   * timer to measure the elapsed time.  This method may return the
   * same value even if the collection count has been incremented
   * if the collection elapsed time is very short.
   *
   * @return the approximate accumulated collection elapsed time
   *         in milliseconds.
   */
  def gcCollectionTime: Long = garbageCollectorMXBeans.map { b: GarbageCollectorMXBean =>
    if (b.isValid)
      b.getCollectionTime
    else
      0
  }.sum

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

  def getHotspotMBean: HotSpotDiagnosticMXBean = {
    val server = ManagementFactory.getPlatformMBeanServer
    ManagementFactory.newPlatformMXBeanProxy(server,
      "com.sun.management:type=HotSpotDiagnostic", classOf[HotSpotDiagnosticMXBean])
  }

  lazy val localProcessId: Int = VitalsNativeMemoryReporter.pid.toInt

  final case
  class VitalsDiskPerformance(
                               diskName: String,
                               readBytes: Long,
                               writeBytes: Long,
                               reads: Long,
                               writes: Long,
                               transferTime: Long
                             )

  final case
  class VitalsNetworkPerformance(
                                  interfaceName: String,
                                  receivedBytes: Long,
                                  sentBytes: Long,
                                  receivedPackets: Long,
                                  sentPackets: Long,
                                  transferTime: Long
                                )

  // Requires glibc - 2.14
  def diskPerformance(): Array[VitalsDiskPerformance] = {
    val diskPerformance = new scala.collection.mutable.ArrayBuffer[VitalsDiskPerformance]
    val si = new SystemInfo
    for (disk <- si.getHardware.getDiskStores.asScala) {
      val diskName = disk.getName
      val readBytes = disk.getReadBytes
      val writeBytes = disk.getWriteBytes
      val reads = disk.getReads
      val writes = disk.getWrites
      val transferTime = disk.getTransferTime
      diskPerformance += VitalsDiskPerformance(diskName, readBytes, writeBytes,
        reads, writes, transferTime)
    }
    diskPerformance.toArray
  }

  // Requires glibc - 2.14
  def networkPerformance(): Array[VitalsNetworkPerformance] = {
    val networkPerformance = new scala.collection.mutable.ArrayBuffer[VitalsNetworkPerformance]
    val si = new SystemInfo
    for (interface <- si.getHardware.getNetworkIFs.asScala) {
      val interfaceName = interface.getName
      val receivedBytes = interface.getBytesRecv
      val sentBytes = interface.getBytesSent
      val receivedPackets = interface.getPacketsRecv
      val sentPackets = interface.getPacketsSent
      val transferTime = interface.getTimeStamp
      networkPerformance += VitalsNetworkPerformance(interfaceName, receivedBytes,
        sentBytes, receivedPackets, sentPackets, transferTime)
    }
    networkPerformance.toArray
  }


}
