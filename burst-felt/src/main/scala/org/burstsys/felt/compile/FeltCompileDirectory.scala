/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile

import org.burstsys.vitals.errors.VitalsException

import java.io.ByteArrayInputStream
import scala.collection.JavaConverters._
import scala.tools.nsc.io._

/**
 * The scala compiler ise set up to use an in-memory 'virtual directory' with
 * 'virtual files' instead of heavy weight physical directories/files
 */
final case
class FeltCompileDirectory() extends VirtualDirectory("(FeltCompileDirectory)", None) {

  /**
   * simple directory structure
   */
  val fileMap = new gnu.trove.map.hash.THashMap[String, VirtualFile]

  /**
   * extract the byte code out of the 'virtual files' in the 'virtual directory'
   * that is used by the internal compiler
   *
   * @return
   */
  def bytecode: Array[(String, Array[Byte])] = {
    fileMap synchronized {
      fileMap.asScala.map {
        case (className, file) =>
          // eventually we want to only get the inner class closures but the constructor asked for the outer class !!!
          val size = file.sizeOption.getOrElse(throw new RuntimeException(""))
          val buffer = new Array[Byte](size)
          val is = file.input.asInstanceOf[ByteArrayInputStream]
          is.read(buffer, 0, size)
          (className.stripSuffix(".class"), buffer)
        case _ => throw VitalsException(s"ruh row")
      }.toArray
    }
  }

  /**
   * clear the virtual directory
   */
  def reset(): Unit = {
    fileMap.clear()
    super.clear()
  }

  override
  def fileNamed(name: String): AbstractFile = {
    fileMap synchronized {
      val file = super.fileNamed(name)
      fileMap put (name, file.asInstanceOf[VirtualFile])
      file
    }
  }
}
