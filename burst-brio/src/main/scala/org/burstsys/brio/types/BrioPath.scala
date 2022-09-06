/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import java.util.StringTokenizer

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes.BrioRelationName
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.language.implicitConversions

object BrioPath {
  final val BrioInvalidPathKey = 0

  final val BrioMissingPathKey: BrioPathKey = -1

  final val pathMap = new java.util.HashMap[String, BrioPath]

  final val splitMap = new java.util.HashMap[String, (String, BrioPath)]

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Paths
  /////////////////////////////////////////////////////////////////////////////////////////////

  type BrioPathName = String
  type BrioPathKey = Int

  implicit final
  def pathToString(path: BrioPath): String = path.fullForm

  implicit final
  def stringToPath(path: String): BrioPath = BrioPath(path)

  final val RootParentPathKey: BrioPathKey = BrioMissingPathKey
  final val BrioPathKeyNotFound: BrioPathKey = BrioMissingPathKey


  @inline final
  def getPath(path: String): BrioPath = {
    pathMap.get(path) match {
      case null => val p = BrioPath(path); pathMap.put(path, p); p
      case p => p
    }
  }

  @inline final
  def split(path: String): (String, BrioPath) = {
    splitMap.get(path) match {
      case null =>
        val index = path.indexOf('.')
        if (index == -1)
          throw VitalsException(s"can't split a single component in a path '$path'")

        splitMap.put(path, (path.substring(0, index), BrioPath(path.substring(index + 1))))

      case s => s
    }
  }

}

final case
class BrioPath(var fullForm: String) extends KryoSerializable {
  def this() = this(null)

  import BrioPath._

  if (fullForm != null) fullForm.intern()

  /**
    * how many components deep is this path
    */
  lazy val depth: BrioPathKey = fullForm.count {
    _ == '.'
  } + 1

  private var _parameterKey: String = _

  def parameterKey: String = _parameterKey

  def hasParameterKey: Boolean = {
    components
    _parameterKey != null
  }

  /**
    * The set of all components in this path as an array
    */
  lazy val components: Array[String] = {
    val result = new collection.mutable.ArrayBuffer[String]
    val tokenizer = new StringTokenizer(fullForm, ".", false)
    while (tokenizer.hasMoreElements) {
      result += tokenizer.nextToken
    }
    val a = result.toArray
    if (a.length > 0) {
      val key = a(a.length - 1).split(Array('[', ']'))
      if (key.length > 1) {
        _parameterKey = key(1).trim
        a(a.length - 1) = key(0).trim
      }
    }
    a
  }

  lazy val formWithoutKey: String = components.foldRight("")(_ + '.' + _).stripSuffix(".")

  /**
    * just the root component of this path
    */
  lazy val root: BrioPath = BrioPath(components(0))

  lazy val leafName: String = components.last

  /**
    * Return the path without any components above this one
    */
  lazy val withoutRoot: BrioPath = {
    fullForm.indexOf('.') match {
      case -1 => BrioPath("")
      case n => BrioPath(fullForm.substring(n + 1))
    }
  }

  /**
    * return the path one level up towards root
    */
  lazy val parent: BrioPath = {
    fullForm.lastIndexOf('.') match {
      case -1 => getPath("")
      case n => getPath(fullForm.substring(0, n))
    }
  }

  /**
    * are there no components in thsi path
    */
  lazy val isEmpty: Boolean = fullForm.isEmpty

  /**
    * return all subpaths that lead to this path
    * i.e. 'a.b.c.d' has 'a', 'a.b', 'a.b.c', and 'a.b.c.d'
    */
  lazy val subpaths: Array[BrioPath] = {
    val paths = new mutable.ArrayBuilder.ofRef[BrioPath]
    // start at root
    var p = BrioPath("")
    // one by one add the next component
    components foreach {
      c =>
        p = p.withLeaf(c)
        paths += p
    }
    paths.result()
  }

  /**
    * add another component onto the end of this path
    *
    * @param component
    * @return
    */
  def withLeaf(component: String): BrioPath = {
    if (components.isEmpty)
      getPath(component)
    else
      getPath(this.fullForm + "." + component)
  }

  def split: (String, BrioPath) = BrioPath.split(this.fullForm)

  def matches(path: BrioPath, relationName: BrioRelationName): Boolean = {
    this.fullForm.equals(path.fullForm + '.' + relationName)
  }

  override def toString: String = fullForm

  def max(that: BrioPath): BrioPath = if (this.depth > that.depth) this else that

  def min(that: BrioPath): BrioPath = if (this.depth < that.depth) this else that

  override def hashCode(): Int = fullForm.hashCode()

  override def equals(obj: scala.Any): Boolean = {
    this.fullForm.equals(obj.asInstanceOf[BrioPath].fullForm)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  def write(k: Kryo, out: Output): Unit = {
    out writeString fullForm
  }

  def read(k: Kryo, in: Input): Unit = {
    fullForm = in.readString
  }

}
