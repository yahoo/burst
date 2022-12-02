/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.properties.VitalsPropertyKey

import java.io.FileWriter
import java.nio.file.{FileSystems, Files, Path, Paths}

package object alloy extends VitalsLogger {
  final val AlloyJsonFileProperty: VitalsPropertyKey = "burst.store.alloy.json.file"
  final val AlloyJsonRootVersionProperty: VitalsPropertyKey = "burst.store.alloy.json.rootVersion"

  final val resourceClassPath = "/org/burstsys/alloy/views"
  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  private
  val tempDir = System.getProperty("java.io.tmpdir")

  final
  def getResourceFile(name: String): Path = {
    try {
      val indexFilePath = FileSystems.getDefault.getPath(tempDir, Paths.get(name).getFileName.toString)
      if (Files.exists(indexFilePath)) {
        Files.delete(indexFilePath)
        // return indexFilePath
      }

      val classPathLocation = s"${resourceClassPath}/$name"
      //  now copy the classpath resource to the tmp folder
      val stream = this.getClass.getResourceAsStream(classPathLocation)
      if (stream == null)
        throw VitalsException(s"resource $classPathLocation not found in classpath")
      val newPath = Paths.get(indexFilePath.toUri)
      Files.copy(stream, newPath)
      newPath
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  final
  def makeJsonFile(name: String, source: String): Path = {
    try {
      val indexFilePath = FileSystems.getDefault.getPath(tempDir, Paths.get(name).getFileName.toString)
      if (Files.exists(indexFilePath)) {
        Files.delete(indexFilePath)
      }

      val f = new FileWriter(indexFilePath.toFile)
      f.write(source)
      f.close()
      indexFilePath
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  final
  def makeJsonFile(name: String, sources: Array[String]): Path = {
    try {
      val indexFilePath = FileSystems.getDefault.getPath(tempDir, Paths.get(name).getFileName.toString)
      if (Files.exists(indexFilePath)) {
        Files.delete(indexFilePath)
      }

      val f = new FileWriter(indexFilePath.toFile)
      f.write("[")
      var first = true
      for (s <- sources) {
        if (first)
          first = false
        else
          f.write(",")
        f.write(s)
      }
      f.write("]")
      f.close()
      indexFilePath
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  final
  def makeJsonFile(name: String, generator: Iterator[BrioPressInstance]): Path = {
    try {
      val indexFilePath = FileSystems.getDefault.getPath(tempDir, Paths.get(name).getFileName.toString)
      if (Files.exists(indexFilePath)) {
        Files.delete(indexFilePath)
      }

      val f = new FileWriter(indexFilePath.toFile)
      f.write("[")
      var first = true
      var i = 0
      for (s <- generator) {
        if (first)
          first = false
        else
          f.write(",")
        f.write(mapper.writeValueAsString(s))
        i += 1
        log debug s"item $i"
      }
      f.write("]")
      f.close()
      indexFilePath
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }
}
