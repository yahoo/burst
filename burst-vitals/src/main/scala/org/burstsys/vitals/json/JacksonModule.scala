/* Copyright·pjfanning,·Licensed·under·the·terms·of·the·Apache·2.0·license.
 * https://github.com/pjfanning/jackson-module-scala-duration/blob/main/src/main/scala/com/github/pjfanning/scala/duration/JacksonModule.scala */

package org.burstsys.vitals.json

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.`type`.TypeModifier
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.{BeanSerializerModifier, Serializers}
import org.burstsys.vitals

import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object JacksonModule {
  lazy val version: Version =
    VersionUtil.parseVersion(vitals.git.buildVersion, "org.burstsys", "burst-vitals")
}

object VersionExtractor {
  def unapply(v: Version): Option[(Int, Int)] = Some(v.getMajorVersion, v.getMinorVersion)
}

trait JacksonModule extends Module {

  private val initializers = Seq.newBuilder[SetupContext => Unit]

  def getModuleName: String = "JacksonModule"

  def version: Version = JacksonModule.version

  def setupModule(context: SetupContext): Unit = {
    initializers.result().foreach(_ apply context)
  }

  protected def +=(init: SetupContext => Unit): this.type = { initializers += init; this }
  protected def +=(ser: Serializers): this.type = this += (_ addSerializers ser)
  protected def +=(deser: Deserializers): this.type = this += (_ addDeserializers deser)
  protected def +=(typeMod: TypeModifier): this.type = this += (_ addTypeModifier typeMod)
  protected def +=(beanSerMod: BeanSerializerModifier): this.type = this += (_ addBeanSerializerModifier beanSerMod)
}
