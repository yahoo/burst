/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.util.Properties

import org.burstsys.vitals
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * helper types for network/disk IO
  */
package object io extends VitalsLogger {

  def sizeBetween(size: ByteSize, low: ByteSize, high: ByteSize): Boolean = {
    (size.inB >= low.inB) && (size.inB < high.inB)
  }

  final val KB: Long = math.pow(2, 10).toLong
  final val MB: Long = math.pow(2, 20).toLong
  final val GB: Long = math.pow(2, 30).toLong
  final val TB: Long = math.pow(2, 40).toLong
  final val PB: Long = math.pow(2, 50).toLong

  implicit class ByteSize(val data: Long) extends AnyVal {

    def inB: Long = data

    def inKB: Long = data / KB

    def inMB: Long = data / MB

    def inGB: Long = data / GB

    def inTB: Long = data / TB

    def inPB: Long = data / PB

    def b: Long = data

    def kb: Long = data * KB

    def mb: Long = data * MB

    def gb: Long = data * GB

    def tb: Long = data * TB

    def pb: Long = data * PB

    /**
      * lower and upper bound are inclusive
      *
      * @param low
      * @param high
      * @return
      */
    def between(low: ByteSize, high: ByteSize): Boolean = {
      (data >= low.inB) && (data <= high.inB)
    }


    override def toString: String = vitals.instrument.prettyByteSizeString(data)
  }

  implicit def intToBytesSize(size: Int): ByteSize = new ByteSize(size)

  implicit def doubleToBytesSize(size: Double): ByteSize = new ByteSize(size.toLong)

  implicit def bytesSizeToDouble(size: ByteSize): Double = size.inB.toDouble

  /*
    final
    def printSnappyCompressionStats(data: Array[Byte]): String = {
      val startCompress = System.nanoTime
      val compressed = org.xerial.snappy.Snappy.compress(data)
      val compressNanos = System.nanoTime - startCompress
      val startInflate = System.nanoTime
      val inflated = org.xerial.snappy.Snappy.uncompress(compressed)
      val inflateNanos = System.nanoTime - startInflate
      s"""
         |***********************************************
         |    compressed: ${data.length} bytes
         |      inflated: ${compressed.length} bytes
         |         ratio: ${((compressed.length.toDouble / data.length.toDouble) * 100).toInt}%
         | compress rate: ${prettyBandwidthString(data.length, compressNanos)}
         |  inflate rate: ${prettyBandwidthString(compressed.length, inflateNanos)}
         |***********************************************
         |""".stripMargin
    }
  */

  implicit def byteBufferToString(buffer: ByteBuffer): String = {
    val oldPosition = buffer.position()
    val r = StandardCharsets.UTF_8.decode(buffer).toString
    buffer.position(oldPosition)
    r
  }

  implicit def stringToByteBuffer(s: String): ByteBuffer = {
    ByteBuffer.wrap(s.getBytes("UTF8"))
  }

  implicit def stringToByteArray(s: String): Array[Byte] = {
    s.getBytes("UTF8")
  }

  implicit def byteArrayToByteBuffer(array: Array[Byte]): ByteBuffer = {
    ByteBuffer.wrap(array)
  }

  implicit def byteArrayToString(array: Array[Byte]): String = {
    new String(array, "UTF8")
  }

  implicit def byteBufferToArray(buffer: ByteBuffer): Array[Byte] = {
    val oldPosition = buffer.position()
    var array = new Array[Byte](buffer.remaining)
    buffer.get(array, 0, array.length)
    buffer.position(oldPosition)
    array
  }

  implicit def byteArrayToByteBuffer(buffer: Option[ByteBuffer]): Array[Byte] = {
    buffer match {
      case None => Array.empty
      case Some(b) => b
    }
  }

  def fileNameWithoutPathOrSuffix(path: String): String = {
    val fileName = path.substring(path.lastIndexOf('/') + 1)
    fileName.substring(0, fileName.lastIndexOf('.'))
  }

  def fileNameWithoutSuffix(path: String): String = {
    path.substring(0, path.lastIndexOf('.'))
  }

  /**
    * return a set of temp folders named with a prefix and optionally
    * delete after exit
    *
    * @param prefix
    * @param count
    * @param deleteAfter
    * @return
    */
  def tempFolderProvide(prefix: String, count: Int, deleteAfter: Boolean): Array[Path] = {
    val tmpFolders = for (i <- 0 until count)
      yield {
        val p = Paths.get(FileUtils.getTempDirectoryPath, f"$prefix-$i%02d")
        FileUtils.forceMkdir(p.toAbsolutePath.toFile)
        p
      }
    if (deleteAfter)
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          tmpFolders.iterator.foreach {
            file => FileUtils.deleteDirectory(file.toAbsolutePath.toFile)
          }
        }
      })
    tmpFolders.toArray
  }

  def readJavaPropertiesFile(input: InputStream): Properties = {
    val properties = new Properties()
    input match {
      case null =>
        throw VitalsException("Input Stream is not valid")
      case stream =>
        properties.load(input)
    }
    properties
  }

  def loadPropertyMapFromJavaPropertiesFile(propertiesFile: String): VitalsPropertyMap = {
    val propertiesStream = getClass.getClassLoader.getResourceAsStream(propertiesFile)
    try {
      val properties = readJavaPropertiesFile(propertiesStream)
      readPropertyMapFromJavaProperties(properties)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(s"Failed to read Java Properties from file", t)
    } finally {
      propertiesStream.close()
    }
  }

  /**
    * Load the properties as system properties
    *
    * @param propertiesFile
    */
  def loadSystemPropertiesFromJavaPropertiesFile(propertiesFile: String): Unit = {
    var propertiesStream: InputStream = null
    try {
      propertiesStream = getClass.getClassLoader.getResourceAsStream(propertiesFile)
      assert(propertiesStream != null, s"could not open stream for '$propertiesFile'")

      val properties = readJavaPropertiesFile(propertiesStream)
      for (propertyName <- properties.stringPropertyNames().asScala) {
        System.setProperty(propertyName, properties.getProperty(propertyName))
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(s"failed to read java properties from file '$propertiesFile'", t)
    } finally {
      if (propertiesStream != null)
        propertiesStream.close()
    }
  }

}
