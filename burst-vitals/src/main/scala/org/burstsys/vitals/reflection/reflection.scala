/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.configuration.burstVitalsReflectionScanPrefixProperty
import org.burstsys.vitals.errors.safely
import org.reflections.Reflections
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import java.lang.annotation.Annotation
import java.net.URL
import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

package object reflection {
  private def urls: Iterable[URL] = {
    val aux: Iterable[URL] = if (burstVitalsReflectionScanPrefixProperty.asOption.isDefined) {
      log info s"Reflection prefix '${burstVitalsReflectionScanPrefixProperty.asOption.get}'"
      ClasspathHelper.forPackage(burstVitalsReflectionScanPrefixProperty.asOption.get).asScala
    } else {
      Iterable.empty
    }
    ClasspathHelper.forPackage(burstPackage).asScala ++ aux
  }

  private def reflectConfig: ConfigurationBuilder = ConfigurationBuilder.build()
    .setExpandSuperTypes(false) // we limit the package search for speed
    .addUrls(urls.asJavaCollection)

  private lazy val reflect: AtomicReference[Reflections] = {
    val ar = new AtomicReference[Reflections]()
    log info s"Reflection scanning urls: ${urls.mkString("(", ",", ")")}"
    ar.set(new Reflections(reflectConfig))
    ar
  }

  def getSubTypesOf[T](typ: Class[T]): Set[Class[_ <: T]] =
    reflect.get.getSubTypesOf(typ).asScala.toSet

  def getPackageSubTypesOf[T](superType: Class[T]): util.Collection[_ <: T] = {
    import scala.reflect.runtime.{universe => ru}
    val rm = ru.runtimeMirror(getClass.getClassLoader)

    val objects = reflect.get.getSubTypesOf(superType).asScala map { packageKlass =>
        // get the module or package instance to be sure properties are loaded into registry
        val symbol = try {
          rm.staticModule(packageKlass.getCanonicalName)
        } catch safely {
          case sre: ScalaReflectionException =>
            log info s"property registry package object `${packageKlass.getCanonicalName}` not found"
            throw sre
        }
        rm.reflectModule(symbol).instance
    }
    objects.asJavaCollection.asInstanceOf[util.Collection[_ <: T]]
  }

  def getTypesAnnotatedWith[T <: Annotation](annotationClass: Class[T]): util.Set[Class[_]] =
    reflect.get.getTypesAnnotatedWith(annotationClass)
}
