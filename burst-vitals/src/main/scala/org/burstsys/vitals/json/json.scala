/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.StringKeySerializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.owasp.html.HtmlPolicyBuilder

package object json extends VitalsLogger {

  def buildJsonMapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .addModule(DurationModule)
    .build()

  /**
   * add an aspect to this type that allows it to export its state as a simple immutable
   * JSON serializable all-state, no methods object. This is used to support REST APIs.
   * Note this does not make it JSON, just an object that can be easily and correctly
   * turned into JSON.
   * <hr/>
   * '''NOTE''' the basic JSON serialization trick
   * is that if you express a property as visible ''state'' i.e. it is a public
   * ''val'' or ''var'' (as would be automatically be true for a ''case'' class parameter) then it will
   * serialized normally into JSON. If you express a property using a pure behavior i.e. a ''def'',
   * then it will '''not''' show up in the JSON object. Any sort of ''private'' or ''protected'' state
   * will '''not''' show up either.
   * <br/>
   * '''NOTE:''' also beware of passing a JSON object that is not a copy. Sometimes the current object is part of a
   * pooled object tree and as such may get reset/modified later in another thread or context of some sort..
   * Generally its safest to always create a copy.
   *
   * @tparam O
   */
  trait VitalsJsonRepresentable[O] extends AnyRef {

    /**
     * return a deep json compatible immutable copy of this object
     *
     * @return
     */
    def toJson: O = this.asInstanceOf[O] // default is simply this object - this will likely only work for case classes with immutable attributes

    /**
     * we assert the possibility of a reduced surface area JSON image for performance
     * and complexity reasons i.e. control how much state is necessary and sufficient to
     * serve the purpose of the JSON consumer while limiting the required BW.
     * @return
     */
    def toJsonLite: O = toJson // default is the same as full json serialization

  }

  trait VitalsJsonObject {
    /**
     * A default implementation for any method, JSON objects should be immutable
     */
    final def jsonMethodException[R]: R = {
      val location = Thread.currentThread().getStackTrace.slice(3, 6).mkString("\n\t")
      throw VitalsException(s"VitalsJsonObjects don't support methods \n\t$location")
    }
  }


  object VitalsJsonSanatizers {
    private val sanatizer = new HtmlPolicyBuilder().toFactory

    private def stripHtmlFrom(str: Any): String = str match {
      case s: String => sanatizer.sanitize(s)
      case _: Int | Long | Double | Float => s"$str"
      case _ => s"unknown type ${str.getClass.getSimpleName}"
    }

    class Values extends ToStringSerializerBase(classOf[Object]) {
      override def valueToString(value: Any): String = stripHtmlFrom(value)
    }

    class Keys extends StringKeySerializer {
      override def serialize(value: Object, gen: JsonGenerator, provider: SerializerProvider): Unit = {
        gen.writeFieldName(stripHtmlFrom(value))
      }
    }
  }

}
