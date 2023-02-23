/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.json

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.`type`.SimpleType
import com.fasterxml.jackson.databind.deser.{Deserializers, KeyDeserializers}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationConfig, DeserializationContext, JavaType, JsonDeserializer, JsonSerializer, KeyDeserializer, SerializationConfig, SerializerProvider}
import com.fasterxml.jackson.databind.ser.Serializers

import scala.concurrent.duration.Duration


object DurationModule extends DurationModule

class DurationModule extends DurationSerializerModule with DurationDeserializerModule

private object DurationSerDeShared {
  val DurationClass: Class[Duration] = classOf[Duration]
  val StringClass: Class[String] = classOf[String]
  lazy val DurationType: SimpleType = SimpleType.constructUnsafe(DurationClass)
}

///////////////////////////////////
// Serialization
///////////////////////////////////

private object FiniteDurationSerializer extends JsonSerializer[Duration] {
  def serialize(value: Duration, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    provider.defaultSerializeValue(value.toString, jgen)
  }
}

private object FiniteDurationKeySerializer extends JsonSerializer[Duration] {

  def serialize(value: Duration, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    val keySerializer = provider.findKeySerializer(DurationSerDeShared.StringClass, null)
    keySerializer.serialize(value.toString, jgen, provider)
  }
}

private object FiniteDurationSerializerResolver extends Serializers.Base {
  override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription): JsonSerializer[Duration] =
    if (DurationSerDeShared.DurationClass.isAssignableFrom(javaType.getRawClass)) {
      FiniteDurationSerializer
    } else {
      null
    }
}

private object FiniteDurationKeySerializerResolver extends Serializers.Base {
  override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription): JsonSerializer[Duration] =
    if (DurationSerDeShared.DurationClass.isAssignableFrom(javaType.getRawClass)) {
      FiniteDurationKeySerializer
    } else {
      null
    }
}

trait DurationSerializerModule extends JacksonModule {
  this += { _ addSerializers FiniteDurationSerializerResolver }
  this += { _ addKeySerializers FiniteDurationKeySerializerResolver }
}


///////////////////////////////////
// Deserialization
///////////////////////////////////

private object FiniteDurationDeserializer extends StdDeserializer[Duration](classOf[Duration]) {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Duration = {
    Option(ctxt.readValue(p, DurationSerDeShared.StringClass)) match {
      case Some(duration) => Duration(duration)
      case _ => null
    }
  }
}

private object FiniteDurationKeyDeserializer extends KeyDeserializer {
  override def deserializeKey(key: String, ctxt: DeserializationContext): AnyRef = {
    Duration(key)
  }
}

private object FiniteDurationDeserializerResolver extends Deserializers.Base {
  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription): JsonDeserializer[Duration] =
    if (DurationSerDeShared.DurationClass isAssignableFrom javaType.getRawClass) {
      FiniteDurationDeserializer
    } else {
      null
    }
}

private object FiniteDurationKeyDeserializerResolver extends KeyDeserializers {
  override def findKeyDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription): KeyDeserializer =
    if (DurationSerDeShared.DurationClass isAssignableFrom javaType.getRawClass) {
      FiniteDurationKeyDeserializer
    } else {
      null
    }
}

trait DurationDeserializerModule extends JacksonModule {
  this += { _ addDeserializers FiniteDurationDeserializerResolver }
  this += { _ addKeyDeserializers FiniteDurationKeyDeserializerResolver }
}
