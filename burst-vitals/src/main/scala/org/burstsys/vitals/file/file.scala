/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Scanner
import scala.jdk.CollectionConverters._

package object file extends VitalsLogger {

  final
  def removePathFilesAndDirsRecursively(root: Path): Unit = {
    if (Files.exists(root))
      Files.walkFileTree(root, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (Files.exists(file))
            Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          if (Files.exists(dir))
            Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })
  }

  /**
    * get text from files with a specific suffix in a classpath. This includes either found in a file folder
    * (as in running in an IDE) or in a JAR file (as in production scenarios).
    *
    * @param clazz  a class in a classloader that has the appropriate resources
    * @param path   a folder 'prefix' - search happens below this in class path heirarchy
    * @param suffix a suffix/file-type
    * @return
    */
  final
  def extractTextFilesFromClasspath(clazz: Class[_], path: String, suffix: String): Array[String] = {
    try {
      val normalizedPath = s"${path.stripPrefix("/")}"
      val url = clazz.getClassLoader.getResource(normalizedPath)
      if (url == null)
        throw VitalsException(s"did not find resource path $path while looking for $suffix files using clazz=${clazz.getName}")

      log debug burstStdMsg(s"path=$path found url=$url")
      if (url.getProtocol.equals("file")) {
        return new File(url.toURI).list.filter(_.endsWith(s".$suffix")) map {
          fileName =>
            val filePath = s"/${normalizedPath.stripSuffix("/")}/$fileName"
            val resourceAsStream = clazz.getResourceAsStream(filePath)
            if (resourceAsStream == null)
              throw VitalsException(s"could not open stream for $filePath for $suffix files")
            new Scanner(resourceAsStream, "UTF-8").useDelimiter("\\A").next
        }
      } else if (url.getProtocol.equals("jar")) {
        val connection = url.openConnection.asInstanceOf[JarURLConnection]
        val names = connection.getJarFile.entries.asScala.filter(
          e => e.getName.startsWith(path) && e.getName.endsWith(suffix)
        ).map(_.getName).toArray
        val loader = clazz.getClassLoader
        val resources = names.map {
          name =>
            new Scanner(loader.getResourceAsStream(s"$name"), "UTF-8").useDelimiter("\\A").next
        }
        return resources
      }
      throw VitalsException(s"did not know how to load url=$url")
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }


}
