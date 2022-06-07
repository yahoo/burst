/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.brio.types.BrioTypes.BrioRelationCount
import org.burstsys.felt.model.reference.names.FeltNameSpace
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code.FeltCode

import scala.collection.mutable
import scala.reflect.ClassTag

abstract
class FeltStdRefResolver[D <: FeltRefDecl : ClassTag] extends FeltRefResolver {

  final override
  def toString: FeltCode = s"${getClass.getSimpleName}(${_namespaceDeclLookup.map(_._1.absoluteName).mkString(", ")})"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _namespaceDeclLookup = new mutable.HashMap[FeltNameSpace, D]

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUBTYPING
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  protected def addResolution(refName: FeltPathExpr, d: D): FeltReference

  protected def addNomination(c: D): Option[FeltReference]

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveAbsolute(name: String): Option[FeltRefDecl] = {
    global.rootNameSpace.lookupAbsolute(name) match {
      case None => None
      case Some(ns) =>
        _namespaceDeclLookup.get(ns) match {
          case None =>
            None
          case Some(d) => Some(d)
        }
    }
  }

  final override
  def resolve(refName: FeltPathExpr): Option[FeltReference] = {
    //    log info s"RESOLVE( resolver='${this.getClass.getSimpleName}' refName='${refName.fullPath}'"
    refName.nameSpace.lookup(refName) match {
      case None =>
        None
      case Some(ns) =>
        _namespaceDeclLookup.get(ns) match {
          case None =>
            None
          case Some(d) =>
            val reference = addResolution(refName, d)
            reference.sync(d)
            reference.resolveTypes
            Some(reference)
        }
    }
  }

  final override def bindingCount: BrioRelationCount = _namespaceDeclLookup.size

  final override def nominate(decl: FeltRefDecl): Unit = {
    decl match {
      case c: D =>
        addNomination(c) match {
          case None =>
          case Some(reference) =>
            decl.refName.reference = Some(reference.resolveTypes)
            reference.sync(decl)
            _namespaceDeclLookup += reference.nameSpace -> decl.asInstanceOf[D]
            c.refName.absolutePath = decl.nameSpace.absoluteName
            c.refName.absolutePath
        }
      case _ =>
    }
  }

}
