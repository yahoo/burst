/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.sql.{Blob, Clob, SQLException}
import org.burstsys.relate.RelateExceptions.{BurstDuplicateKeyException, BurstSqlException}
import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.vitals.stats._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._

import javax.sql.rowset.serial.{SerialBlob, SerialClob}
import scalikejdbc.{ConnectionPoolFactoryRepository, NoExtractor, SQL}

import scala.language.implicitConversions

package object relate extends VitalsLogger {

  type RelatePk = Long

  type TableCreateSql = SQL[Nothing, NoExtractor]


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Connection Pooling
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  sealed case class RelatePool(kind: String)

  object RelateBonecpPool extends RelatePool(ConnectionPoolFactoryRepository.BONECP)

  object RelateDbcpPool extends RelatePool(ConnectionPoolFactoryRepository.COMMONS_DBCP)

  object RelateDbcp2Pool extends RelatePool(ConnectionPoolFactoryRepository.COMMONS_DBCP2)

  object RelateCustomDbcp2Pool extends RelatePool("burst-commons-dbcp2")

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Hex Strings
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def stringToHexString(arg: String): String = {
    String.format("X'%x'", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)))
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ByteBuffer
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit def blobToByteBuffer(blob: java.sql.Blob): ByteBuffer = {
    ByteBuffer.wrap(blobToArray(blob))
  }

  implicit def optionByteBufferToBlob(buffer: Option[ByteBuffer]): Blob = {
    buffer match {
      case None => null
      case Some(dp) => byteBufferToBlob(dp)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Clobs (getClob doesn't work in derby so these are pretty useless...
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit def stringToClob(s: String): java.sql.Clob = {
    new SerialClob(s.toCharArray)
  }

  implicit def clobToString(clob: java.sql.Clob): String = {
    clob.getSubString(1, clob.length.toInt)
  }

  implicit def clobToOptionPropertyMap(clob: Clob): Option[VitalsPropertyMap] = {
    clob match {
      case null => None
      case c => Some(stringToPropertyMap(clobToString(c)))
    }
  }

  implicit def optionPropertyMapToClob(properties: Option[VitalsPropertyMap]): Clob = {
    properties match {
      case None => null
      case Some(p) => propertyMapToString(p)
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Blobs
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit def blobToOptionByteBuffer(blob: Blob): Option[ByteBuffer] = {
    blob match {
      case null => None
      case b => Some(b)
    }
  }

  implicit def blobToArray(blob: Blob): Array[Byte] = {
    val input = blob.getBinaryStream
    val output = new ByteArrayOutputStream()
    var buffer = new Array[Byte](512)
    var continue = true
    while (continue) {
      val count = input.read(buffer)
      if (count == -1) {
        continue = false
      } else {
        output.write(buffer, 0, count)
      }
    }
    output.toByteArray
  }

  implicit def stringToBlob(s: String): java.sql.Blob = {
    new SerialBlob(s.getBytes(StandardCharsets.UTF_8))
  }

  implicit def byteBufferToString(buffer: ByteBuffer): String = {
    val oldPosition = buffer.position()
    val r = StandardCharsets.UTF_8.decode(buffer).toString
    buffer.position(oldPosition)
    r
  }
  implicit def byteBufferToBlob(buffer: ByteBuffer): java.sql.Blob = {
    val s = byteBufferToString(buffer)
    val oldPosition = buffer.position()
    var array = new Array[Byte](buffer.remaining)
    buffer.get(array, 0, array.length)
    buffer.position(oldPosition)
    new SerialBlob(array)
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Strings
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit def optionStringToString(string: Option[String]): String = {
    string match {
      case None => null
      case Some(s) => s
    }
  }

  implicit def stringToFkList(s: String): Seq[RelatePk] = {
    if (s.trim.isEmpty) Seq()
    else
      s.split(",").map(_.trim.toLong).toSeq
  }

  implicit def fkListToString(l: Seq[RelatePk]): String = {
    if (l.isEmpty) ""
    else l.mkString(",").trim
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SQL exception handlers
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def throwMappedException[T](dialect: RelateDialect): PartialFunction[Throwable, T] = {
    case ex: SQLException => throw dialect.mappedSqlException(ex)
  }

  def handleSqlException[T](dialect: RelateDialect)(handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    //If it's an sql exception remap it and let the caller handle it or rethrow it if there is no handler defined
    case ex: SQLException =>
      val remapped = dialect.mappedSqlException(ex)
      if (handler.isDefinedAt(remapped))
        handler(remapped)
      else throw remapped

    case ex: BurstSqlException if handler.isDefinedAt(ex) => handler(ex)
  }


}
