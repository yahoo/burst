/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.io.ObjectInput
import java.io.ObjectOutput
import java.util
import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.language.existentials
import scala.language.higherKinds
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.reflect.classTag


package object properties extends VitalsLogger {

  final val javaBooleanName: String = classOf[java.lang.Boolean].getName
  final val javaByteName: String = classOf[java.lang.Byte].getName
  final val javaShortName: String = classOf[java.lang.Short].getName
  final val javaIntegerName: String = classOf[java.lang.Integer].getName
  final val javaLongName: String = classOf[java.lang.Long].getName
  final val javaDoubleName: String = classOf[java.lang.Double].getName
  final val javaStringName: String = classOf[java.lang.String].getName

  final val scalaBooleanName: String = classOf[scala.Boolean].getName
  final val scalaByteName: String = classOf[scala.Byte].getName
  final val scalaShortName: String = classOf[scala.Short].getName
  final val scalaIntegerName: String = classOf[scala.Int].getName
  final val scalaLongName: String = classOf[scala.Long].getName
  final val scalaDoubleName: String = classOf[scala.Double].getName
  final val scalaStringName: String = classOf[java.lang.String].getName
  final val scalaDurationName: String = classOf[scala.concurrent.duration.Duration].getName
  final val scalaFiniteDurationName: String = classOf[scala.concurrent.duration.FiniteDuration].getName

  final val arrayByteName: String = classOf[Array[scala.Byte]].getName
  final val arrayShortName: String = classOf[Array[scala.Short]].getName
  final val arrayIntegerName: String = classOf[Array[scala.Int]].getName
  final val arrayLongName: String = classOf[Array[scala.Long]].getName
  final val arrayDoubleName: String = classOf[Array[scala.Double]].getName

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Type definitions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  type VitalsPropertyAtomicDataType = A forSome {type A >: AnyVal; type String}

  type VitalsPropertyArrayDataType = Array[VitalsPropertyComplexDataType]

  type VitalsPropertyComplexDataType = T forSome {type T >: VitalsPropertyAtomicDataType; type PropertyArrayDataType}

  type VitalsPropertyKey = String
  type VitalsPropertyValue = String

  type VitalsMutablePropertyMap = mutable.HashMap[VitalsPropertyKey, VitalsPropertyValue]
  type VitalsPropertyMap = scala.collection.Map[VitalsPropertyKey, VitalsPropertyValue]
  type VitalsLabelsMap = scala.collection.Map[VitalsPropertyKey, VitalsPropertyValue]
  type VitalsExtendedPropertyMap = scala.collection.Map[VitalsPropertyKey, VitalsPropertyComplexDataType]

  // TODO THIS NEEDS TO BE MOVED OUT OF COMMON
  type BurstMotifFilter = Option[String]

  final def readPropertyMap(input: Input): VitalsPropertyMap = {
    try {
      val size = input.readInt
      val map = new mutable.HashMap[VitalsPropertyKey, VitalsPropertyValue]
      var i = 0
      while (i < size) {
        map += input.readString -> input.readString
        i += 1
      }
      map.toMap
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  final def writePropertyMap(output: Output, map: VitalsPropertyMap): Unit = {
    try {
      output writeInt map.size
      map.foreach {
        case (k, v) =>
          output writeString k
          output writeString v
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // System properties and environmental variables
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * get a burst property from the java environment
   *
   * @param name    the name of the environment variable
   * @param default a fallback value if the variable is not defined
   * @return the value from the environment or the default
   */
  final def environment[C <: VitalsPropertyAtomicDataType : ClassTag](name: String, default: C): C =
    tryConvert(name, System.getenv(name), default)

  /**
   * get a burst property from the java system properties.
   *
   * @param name    the name of the system properties variable
   * @param default a fallback value if the variable is not defined
   * @return the value from the system properties or the default
   */
  final def property[C <: VitalsPropertyAtomicDataType : ClassTag](name: String, default: C): C =
    tryConvert(name, System.getProperty(name), default)


  private final
  def tryConvert[C <: VitalsPropertyAtomicDataType : ClassTag](parameter: String, value: String, default: C): C = {
    try {
      value match {
        case null => default
        case v => handleStringConversions(parameter, v, default)
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"parameter=$parameter bad"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }

  }

  final def setProperty[C <: VitalsPropertyAtomicDataType : ClassTag](parameter: String, v: C = 0): Unit = {
    System.setProperty(parameter, v.toString)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Property Map Serde
  /////////////////////////////////////////////////////////////////////////////////////////////

  final def deserializePropertyMap(input: ObjectInput): VitalsPropertyMap = {
    val result = new mutable.HashMap[VitalsPropertyKey, VitalsPropertyValue]()
    val entries = input.readInt
    var i = 0
    while (i < entries) {
      result += input.readUTF() -> input.readUTF
      i += 1
    }
    result.toMap
  }

  final def serializePropertyMap(output: ObjectOutput, properties: VitalsPropertyMap): Unit = {
    output writeInt properties.size
    properties foreach {
      case (k, v) =>
        output writeUTF k
        output writeUTF v
    }
  }

  final def readPropertyMapFromJavaProperties(javaProperties: Properties): VitalsPropertyMap = {
    val result = new mutable.HashMap[VitalsPropertyKey, VitalsPropertyValue]()
    val propertyNames = javaProperties.stringPropertyNames().asScala

    for (propertyName <- propertyNames) {
      result += propertyName -> javaProperties.getProperty(propertyName)
    }
    result.toMap
  }

  final def writePropertyMapToJavaProperties(javaProperties: Properties, properties: VitalsPropertyMap): Unit = {
    properties foreach {
      case (k, v) =>
        javaProperties.setProperty(k, v)
    }
  }

  final def readPropertyMapFromKryo(input: Input): VitalsPropertyMap = {
    val result = new util.HashMap[VitalsPropertyKey, VitalsPropertyValue]
    val entries = input.readInt
    var i = 0
    while (i < entries) {
      result.put(input.readString, input.readString)
      i += 1
    }
    result.asScala.toMap
  }

  final def writePropertyMapToKryo(output: Output, properties: VitalsPropertyMap): Unit = {
    output writeInt properties.size
    val keys = properties.keys.iterator
    while (keys.hasNext) {
      val key = keys.next()
      output writeString key
      output writeString properties(key)
    }
  }

  final
  implicit def propertyMapToString(properties: VitalsPropertyMap): String = {
    properties.filter {
      case (k, v) => k != null && v != null && !k.isEmpty && !v.isEmpty
    }.foldRight("") {
      case ((k, v), r) => s"$r ${k.trim}=${v.trim}; \n"
    }.trim
  }

  implicit def optionalPropertyMapToString(properties: Option[VitalsPropertyMap]): String = {
    properties.map(propertyMapToString).getOrElse("")
  }

  implicit def stringToPropertyMap(string: String): VitalsPropertyMap = {
    if (string == null || string == "") Map.empty
    else string.trim.split(';').foldLeft(Map[VitalsPropertyKey, VitalsPropertyValue]()) {
      case (map, entry) =>
        val kv = entry.trim.split("=")
        map + (kv(0).trim -> kv.tail.mkString("=").trim)
    }
  }

  implicit def stringToOptionalPropertyMap(string: String): Option[VitalsPropertyMap] = {
    if (string == "" || string == null) None else Some(stringToPropertyMap(string))
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Extended Property Maps
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * extensions for basic (string -> string) property maps.
   */
  implicit class VitalsRichPropertyMap(val map: VitalsPropertyMap) {

    final def extend: VitalsExtendedPropertyMap = {
      val result = new mutable.HashMap[VitalsPropertyKey, VitalsPropertyComplexDataType]()
      map foreach { case (k, v) => result += k -> convertToExtended(v.trim) }
      result.toMap
    }

    final def convertToExtended(value: VitalsPropertyValue): VitalsPropertyComplexDataType = {
      lazy val tag = s"VitalsRichPropertyMap.convertToExtended(value=$value)"
      try {
        return value.toBoolean
      } catch safely {
        case _: IllegalArgumentException => // pass
      }
      try {
        return value.toInt
      } catch safely {
        case _: NumberFormatException => // pass
      }
      try {
        return value.toLong
      } catch safely {
        case _: NumberFormatException => // pass
      }
      try {
        return value.toDouble
      } catch safely {
        case _: NumberFormatException => // pass
      }
      try {
        return Duration(value)
      } catch safely {
        case _: NumberFormatException => // pass
      }
      if (!value.isInstanceOf[String]) {
        throw VitalsException(s"VITALS_PROP_CANT_PARSE $tag")
      }
      val s = value.trim
      if (s.startsWith("{") && s.endsWith("}")) {
        val ss = s.substring(1, s.lastIndexOf('}')).split(',')
        val values = new ArrayBuffer[Any]
        ss foreach {
          sv => values += convertToExtended(sv.trim)
        }
        // try for strongly typed homogeneous collections
        if (values.forall(_.isInstanceOf[Boolean])) {
          return values.map(_.asInstanceOf[Boolean]).toArray
        } else if (values.forall(_.isInstanceOf[Int])) {
          return values.map(_.asInstanceOf[Int]).toArray
        } else if (values.forall(_.isInstanceOf[Long])) {
          return values.map(_.asInstanceOf[Long]).toArray
        } else if (values.forall(_.isInstanceOf[Double])) {
          return values.map(_.asInstanceOf[Double]).toArray
        } else if (values.forall(_.isInstanceOf[java.lang.String])) {
          return values.map(_.asInstanceOf[java.lang.String]).toArray
        } else if (values.forall(_.isInstanceOf[String])) {
          return values.map(_.asInstanceOf[String]).toArray
        } else if (values.forall(_.isInstanceOf[Duration])) {
          return values.map(_.asInstanceOf[Duration]).toArray
        }
        values.toArray
      } else s
    }

    private def convertPropertyValue[C <: VitalsPropertyAtomicDataType : ClassTag](value: VitalsPropertyValue, tag: String): C = {
      classTag[C].runtimeClass match {
        case b if b == classOf[Boolean] => value.toBoolean.asInstanceOf[C]
        case s if s == classOf[Short] => value.toShort.asInstanceOf[C]
        case i if i == classOf[Int] => value.toInt.asInstanceOf[C]
        case l if l == classOf[Long] => value.toLong.asInstanceOf[C]
        case d if d == classOf[Double] => value.toDouble.asInstanceOf[C]
        case s if s == classOf[java.lang.String] => value.asInstanceOf[C]
        case s if s == classOf[String] => value.asInstanceOf[C]
        case d if d == classOf[Duration] => Duration(value).asInstanceOf[C]
        case o => throw VitalsException(s"VITALS_PROP_UNKNOWN_TYPE type=${o.getName} $tag")
      }
    }

    final def getValueOrThrow[C <: VitalsPropertyAtomicDataType : ClassTag](key: VitalsPropertyKey): C = {
      val tag = s"VitalsRichPropertyMap.getValueOrThrow(key=$key))"
      val v = map.getOrElse(key, throw VitalsException(s"VITALS_PROP_UNKNOWN_KEY  $tag"))
      convertPropertyValue(v, tag)
    }

    final def getValueOrDefault[C <: VitalsPropertyAtomicDataType : ClassTag](key: VitalsPropertyKey, defaultValue: VitalsPropertyValue): C = {
      val tag = s"VitalsRichPropertyMap.getValueOrDefault(property=$key)"
      val v = map.getOrElse(key, defaultValue)
      convertPropertyValue(v, tag)
    }

  }

  /**
   * extensions for extended property maps
   */
  implicit class VitalsRichExtendedPropertyMap(val map: VitalsExtendedPropertyMap) extends AnyVal {

    private def castValue[C <: VitalsPropertyComplexDataType : ClassTag](tag: String, value: Any): C = {
      val targetClass = classTag[C].runtimeClass.getName
      val sourceClass = value.getClass.getName

      if (targetClass == sourceClass)
        return value.asInstanceOf[C]

      (sourceClass, targetClass) match {
        case (`javaBooleanName`, `scalaBooleanName`) =>
          value.asInstanceOf[java.lang.Boolean].asInstanceOf[C]

        case (`javaIntegerName`, `scalaIntegerName`) =>
          value.asInstanceOf[java.lang.Integer].toInt.asInstanceOf[C]
        case (`javaIntegerName`, `scalaLongName`) =>
          value.asInstanceOf[java.lang.Integer].toLong.asInstanceOf[C]
        case (`javaIntegerName`, `scalaDoubleName`) =>
          value.asInstanceOf[java.lang.Integer].toDouble.asInstanceOf[C]

        case (`javaDoubleName`, `scalaIntegerName`) =>
          value.asInstanceOf[java.lang.Double].toInt.asInstanceOf[C]
        case (`javaDoubleName`, `scalaLongName`) =>
          value.asInstanceOf[java.lang.Double].toLong.asInstanceOf[C]
        case (`javaDoubleName`, `scalaDoubleName`) =>
          value.asInstanceOf[java.lang.Double].toDouble.asInstanceOf[C]

        case (`javaLongName`, `scalaIntegerName`) =>
          value.asInstanceOf[java.lang.Long].toInt.asInstanceOf[C]
        case (`javaLongName`, `scalaLongName`) =>
          value.asInstanceOf[java.lang.Long].toLong.asInstanceOf[C]
        case (`javaLongName`, `scalaDoubleName`) =>
          value.asInstanceOf[java.lang.Long].toDouble.asInstanceOf[C]

        case (`scalaFiniteDurationName`, `scalaDurationName`) =>
          value.asInstanceOf[FiniteDuration].asInstanceOf[C]
        case (`scalaDurationName`, `scalaFiniteDurationName`) =>
          value.asInstanceOf[Duration].asInstanceOf[C]

        case (_, `javaStringName`) | (_, `scalaStringName`) =>
          value.toString.asInstanceOf[C]

        case (`arrayByteName`, `arrayShortName`) =>
          value.asInstanceOf[Array[Byte]].map(_.toShort).asInstanceOf[C]
        case (`arrayByteName`, `arrayIntegerName`) =>
          value.asInstanceOf[Array[Byte]].map(_.toInt).asInstanceOf[C]
        case (`arrayByteName`, `arrayLongName`) =>
          value.asInstanceOf[Array[Byte]].map(_.toLong).asInstanceOf[C]
        case (`arrayByteName`, `arrayDoubleName`) =>
          value.asInstanceOf[Array[Byte]].map(_.toDouble).asInstanceOf[C]

        case (`arrayShortName`, `arrayByteName`) =>
          value.asInstanceOf[Array[Short]].map(_.toByte).asInstanceOf[C]
        case (`arrayShortName`, `arrayIntegerName`) =>
          value.asInstanceOf[Array[Short]].map(_.toInt).asInstanceOf[C]
        case (`arrayShortName`, `arrayLongName`) =>
          value.asInstanceOf[Array[Short]].map(_.toLong).asInstanceOf[C]
        case (`arrayShortName`, `arrayDoubleName`) =>
          value.asInstanceOf[Array[Short]].map(_.toDouble).asInstanceOf[C]

        case (`arrayIntegerName`, `arrayByteName`) =>
          value.asInstanceOf[Array[Int]].map(_.toByte).asInstanceOf[C]
        case (`arrayIntegerName`, `arrayShortName`) =>
          value.asInstanceOf[Array[Int]].map(_.toShort).asInstanceOf[C]
        case (`arrayIntegerName`, `arrayLongName`) =>
          value.asInstanceOf[Array[Int]].map(_.toLong).asInstanceOf[C]
        case (`arrayIntegerName`, `arrayDoubleName`) =>
          value.asInstanceOf[Array[Int]].map(_.toDouble).asInstanceOf[C]

        case (`arrayDoubleName`, `arrayByteName`) =>
          value.asInstanceOf[Array[Double]].map(_.toByte).asInstanceOf[C]
        case (`arrayDoubleName`, `arrayShortName`) =>
          value.asInstanceOf[Array[Double]].map(_.toShort).asInstanceOf[C]
        case (`arrayDoubleName`, `arrayIntegerName`) =>
          value.asInstanceOf[Array[Double]].map(_.toInt).asInstanceOf[C]
        case (`arrayDoubleName`, `arrayLongName`) =>
          value.asInstanceOf[Array[Double]].map(_.toLong).asInstanceOf[C]


        case _ =>
          throw VitalsException(s"VITALS_PROP_UNKNOWN_TYPE $tag sourceClass=$sourceClass -> targetClass=$targetClass value=$value")
      }

    }

    final def getValueOrThrow[C <: VitalsPropertyComplexDataType : ClassTag](property: VitalsPropertyKey): C = {
      lazy val tag = s"VitalsRichExtendedPropertyMap.getValueOrThrow(key=$property))"
      val v = map.getOrElse(property, throw VitalsException(s"VITALS_PROP_UNKNOWN_KEY $tag"))
      castValue(tag, v)
    }

    final def getValueOrDefault[C <: VitalsPropertyComplexDataType : ClassTag](property: VitalsPropertyKey, default: C): C = {
      lazy val tag = s"VitalsRichExtendedPropertyMap.getValueOrDefault(key=$property)"
      map.get(property) match {
        case None => default
        case Some(v) => castValue(tag, v)
      }
    }

    final def getValueOrProperty[C <: VitalsPropertyComplexDataType : ClassTag](property: VitalsPropertyKey, default: VitalsPropertySpecification[C]): C = {
      lazy val tag = s"VitalsRichExtendedPropertyMap.getValueOrDefault(key=$property)"
      map.get(property) match {
        case None => default.getOrThrow
        case Some(v) => castValue(tag, v)
      }
    }

    final def restrict: VitalsPropertyMap = {
      val result = new mutable.HashMap[VitalsPropertyKey, VitalsPropertyValue]()
      map foreach { case (k, v) => result += k -> convertFromExtended(v) }
      result.toMap
    }

    final def ++(source: VitalsExtendedPropertyMap): VitalsExtendedPropertyMap = {
      map ++ source
    }

    final def convertFromExtended(value: VitalsPropertyComplexDataType): VitalsPropertyValue = {
      lazy val tag = s"VitalsRichExtendedPropertyMap.convertFromExtended(value=$value)"
      value match {
        case b: Boolean => b.toString
        case i: Int => i.toString
        case l: Long => l.toString
        case d: Double => d.toString
        case s: String => s
        case d: Duration => d.toString
        case v: Array[Boolean] => v.mkString("{", ", ", "}")
        case v: Array[Byte] => v.mkString("{", ", ", "}")
        case v: Array[Short] => v.mkString("{", ", ", "}")
        case v: Array[Int] => v.mkString("{", ", ", "}")
        case v: Array[Long] => v.mkString("{", ", ", "}")
        case v: Array[Double] => v.mkString("{", ", ", "}")
        case v: Array[String] => v.mkString("{", ", ", "}")
        case v: Array[Any] => v.map(convertFromExtended).mkString("{", ", ", "}")
        case t => throw VitalsException(s"VITALS_PROP_NO_CONVERT $tag type=${t.getClass.getName}")
      }
    }

  }

  private
  def handleStringConversions[C <: VitalsPropertyAtomicDataType : ClassTag](
                                                                             parameter: String,
                                                                             stringValue: VitalsPropertyValue,
                                                                             defaultValue: C): C = {
    lazy val tag = s"VitalsRichExtendedPropertyMap.handleStringConversions(parameter=$parameter, stringValue=$stringValue, defaultValue=$defaultValue))"
    try {
      val e = classTag[C].runtimeClass
      if (e == classOf[Boolean]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toBoolean.asInstanceOf[C]
      } else if (e == classOf[Long]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toLong.asInstanceOf[C]
      } else if (e == classOf[Int]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toInt.asInstanceOf[C]
      } else if (e == classOf[Double]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toDouble.asInstanceOf[C]
      } else if (e == classOf[java.lang.String]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toString.asInstanceOf[C]
      } else if (e == classOf[String]) {
        if (stringValue.isEmpty) return defaultValue
        stringValue.toString.asInstanceOf[C]
      } else if (e == classOf[Duration]) {
        if (stringValue.isEmpty) return defaultValue
        Duration(stringValue.toString).asInstanceOf[C]
      } else
        throw VitalsException(s"VITALS_PROP_NO_SUPPORT type=$e $tag")
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"VITALS_PROP_CONVERT_FAIL $t  $tag", t)
        throw t
    }
  }

}
