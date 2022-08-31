/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.JavaSerializer
import org.burstsys.vitals.logging._

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps

package object kryo extends VitalsLogger {

  /**
   * how long to wait in queue before creating a new Kryo Codec
   */
  final val kryoPollWait = 5 milliseconds

  type VitalsKryoKey = Int

  type VitalsKryoClass = Class[_]

  type VitalsKryoClassPair = (VitalsKryoKey, VitalsKryoClass)

  private
  lazy val kryoClasses: Array[VitalsKryoClassPair] =
    reflection.getSubTypesOf(
      classOf[VitalsKryoCatalogProvider]
    ).asScala.toList.flatMap(_.getDeclaredConstructor().newInstance().kryoClasses).sortBy(_._1).toArray

  private val codecQueue: LinkedBlockingQueue[Kryo] = new LinkedBlockingQueue[Kryo]()

  private val codecCount = new AtomicLong()

  /**
   * Must be called to return kryo object to pool when serialization is complete
   *
   * @param k
   */
  final
  def releaseKryo(k: Kryo): Unit = codecQueue put k

  /**
   * We cache Kryo instances to make sure we never have to lock
   *
   * @return
   */
  final
  def acquireKryo: Kryo = {
    lazy val tag = s"VitalsKryo.acquireKryo"
    // wait a little bit for already instantiated one
    val k = codecQueue.poll(kryoPollWait.toMillis, TimeUnit.MILLISECONDS)
    if (k == null) {
      // if its slow, just create your own
      log info s"KRYO_CODEC_CREATE #${codecCount.getAndIncrement} $tag"
      val kryo = new Kryo()
      kryo.setRegistrationRequired(true)
      kryo.setReferences(false)
      kryo.setInstantiatorStrategy(new org.objenesis.strategy.StdInstantiatorStrategy)
      // add in some default java types
      kryo.register(classOf[Throwable], new JavaSerializer)
      kryoClasses foreach {
        case (key, klass) => kryo.register(klass, key)
      }
      kryo
    } else k
  }

}
