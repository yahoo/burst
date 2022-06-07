/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.graph

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.felt.model.tree.code._

import scala.language.postfixOps

trait FeltRouteTransition extends Any {

  /**
   * TODO
   *
   * @param index
   * @return
   */
  def apply(index: Int): FeltRouteEdge

  /**
   * TODO
   *
   * @param index
   * @param value
   */
  def update(index: Int, value: FeltRouteEdge): Unit

  /**
   * TODO
   *
   * @return
   */
  def edges: Array[FeltRouteEdge]

  /**
   * TODO
   *
   * @return
   */
  def length: Int

  def generateCode(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltRouteTransition {

  val transitionClassName: String = classOf[FeltRouteTransition].getName

  def apply(data: Array[FeltRouteEdge]): FeltRouteTransition = FeltRouteTransitionContext(data)

  def apply(): FeltRouteTransition = FeltRouteTransitionContext(Array.empty)

}

/**
 * TODO
 *
 * @param edges
 */
private
final case
class FeltRouteTransitionContext(var edges: Array[FeltRouteEdge]) extends AnyRef with KryoSerializable with FeltRouteTransition {
  // TODO change this to AnyVal (value class)

  import FeltRouteTransition._

  override def toString: String = {
    edges.indices.map {
      i =>
        val e = edges(i)
        if (e == null) s"FROM($i) TO NONE"
        else s"FROM($i) TO $e"

    }.mkString("\n\t\t", "\n\t\t", "")
  }

  override
  def length: Int = edges.length

  override
  def apply(index: Int): FeltRouteEdge = edges(index)

  override
  def update(index: Int, value: FeltRouteEdge): Unit = edges(index) = value


  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    def edgesCode(implicit cursor: FeltCodeCursor): FeltCode = edges.map {
      case null =>
        s"""|
            |${I2}null""".stripMargin
      case e => e.generateCode(cursor indentRight 1)
    }.mkString(",")
    s"""
       |$I$transitionClassName(
       |${I2}Array[${FeltRouteEdge.edgeClassName}](${edgesCode(cursor indentRight 1)}
       |$I2)
       |$I)""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeInt edges.length
    var i = 0
    while (i < edges.length) {
      val d: FeltRouteEdge = edges(i)
      if (d == null) {
        output writeBoolean false
      } else {
        output writeBoolean true
        d.asInstanceOf[KryoSerializable].write(kryo, output)
      }
      i += 1
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    val length = input.readInt
    edges = new Array[FeltRouteEdge](length)
    var i = 0
    while (i < length) {
      if (input.readBoolean) {
        edges(i) = FeltRouteEdge()
        edges(i).asInstanceOf[KryoSerializable].read(kryo, input)
      }
      i += 1
    }
  }

}
