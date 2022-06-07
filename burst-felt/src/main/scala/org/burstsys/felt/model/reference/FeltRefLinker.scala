/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef.FeltBrioStdRefResolver
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeRef.FeltCubeRefResolver
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggRef.FeltCubeAggRefResolver
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimRef.FeltCubeDimRefResolver
import org.burstsys.felt.model.collectors.route.decl.FeltRouteRef.FeltRouteRefResolver
import org.burstsys.felt.model.collectors.route.decl.visit.FeltRoutePathsRef.FeltRoutePathsRefResolver
import org.burstsys.felt.model.collectors.route.decl.visit.FeltRouteStepsRef.FeltRouteStepsRefResolver
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletMembersRef.FeltTabletMembersRefResolver
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletRef.FeltTabletRefResolver
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef.FeltGlobalVarRefResolver
import org.burstsys.felt.model.variables.local.ref.FeltLocVarRef.FeltLocVarRefResolver
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef.FeltParamRefResolver

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

/**
 * global manager for linkage from reference instances to referencable artifacts
 *
 * @param additionalResolvers non default reference resolvers
 */
final case
class FeltRefLinker(global: FeltGlobal, additionalResolvers: FeltRefResolver*) {


  private val _resolvers = new mutable.HashSet[FeltRefResolver]

  private val defaultResolvers = Array(
    FeltBrioStdRefResolver(global), FeltGlobalVarRefResolver(global),
    FeltLocVarRefResolver(global), FeltParamRefResolver(global),
    FeltCubeRefResolver(global), FeltCubeAggRefResolver(global), FeltCubeDimRefResolver(global),
    FeltRouteRefResolver(global), FeltRoutePathsRefResolver(global), FeltRouteStepsRefResolver(global),
    FeltTabletRefResolver(global), FeltTabletMembersRefResolver(global)
  )

  _resolvers ++= defaultResolvers
  _resolvers ++= additionalResolvers

  def lookupResolver[R <: FeltRefResolver : ClassTag]: Option[R] = {
    _resolvers.find(_.getClass == classTag[R].runtimeClass) match {
      case None => None
      case Some(r) => Some(r.asInstanceOf[R])
    }
  }

  def nominate(declaration: FeltRefDecl): Unit = {
    _resolvers.foreach(_.nominate(declaration))
  }

  def nominate[D <: FeltRefDecl](declarations: Seq[FeltRefDecl]): Unit = {
    declarations.foreach(d => _resolvers.foreach(_.nominate(d)))
  }

  def lookupDeclFromAbsoluteOrThrow[R <: FeltRefDecl : ClassTag](name: String): R = lookupDeclFromAbsolute(name) match {
    case None =>
      throw FeltException(FeltLocation(), s"FELT_REF_LINKER_ABS_LOOKUP_FAIL name=$name")
    case Some(r) => r match {
      case ref: R => ref
      case ref =>
        throw FeltException(FeltLocation(), s"FELT_REF_LINKER_ABS_CAST_FAIL name=$name")
    }
  }

  def lookupDeclFromAbsolute(name: String): Option[FeltRefDecl] = {
    // scan through the resolvers at this level looking for someone that can help
    _resolvers.foreach(resolver =>
      resolver.resolveAbsolute(name) match {
        case None =>
        case Some(r) => return Some(r)
      })
    None
  }

  def lookupReference(path: FeltPathExpr): Option[FeltReference] = {
    // scan through the resolvers at this level looking for someone that can help
    _resolvers.foreach(resolver =>
      resolver.resolve(path) match {
        case None =>
        case Some(r) => return Some(r)
      })
    None
  }


}
