/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.hydra.utils
import org.burstsys.eql.generators.{ActionSourceGenerator, toActionGenerator}
import org.burstsys.motif.motif.tree.data.{PathAccessor, ValueMapBinding, ValueVectorBinding}
import org.burstsys.motif.paths.funnels.{FunnelPathBase, FunnelPathFieldPath, FunnelPathsPath, FunnelStepFieldPath, FunnelStepsPath}
import org.burstsys.motif.paths.schemas.{MapKeyPath, MapLookupPath, MapValuePath, ValueVectorValuePath}

class RoutePathGenerator(funnelPath: FunnelPathBase) extends ActionSourceGenerator {
    override def generateSource()(implicit context: GlobalContext): utils.CodeBlock =
      funnelPath match {
        case path: FunnelPathFieldPath =>
          // funnel.paths
          path.getField match {
            case FunnelPathFieldPath.Field.ORDINAL =>
              s"routeVisitPathOrdinal(${path.getFunnel.getName})"
            case FunnelPathFieldPath.Field.ENDTIME => ???
            case FunnelPathFieldPath.Field.STARTTIME => ???
            case FunnelPathFieldPath.Field.ISFIRST =>
              s"routeVisitPathIsFirst(${path.getFunnel.getName})"
            case FunnelPathFieldPath.Field.ISLAST =>
              s"routeVisitStepIsLastInPath(${path.getFunnel.getName})"
            case FunnelPathFieldPath.Field.ISCOMPLETE =>
              s"routeVisitPathIsComplete(${path.getFunnel.getName})"
          }
        case path: FunnelPathsPath =>
          throw new IllegalStateException(s"cannot generate path ${path.getPathAsString}")
        case path: FunnelStepFieldPath => path.getField match {
          // funnel.paths.steps
          case FunnelStepFieldPath.Field.ID =>
            s"routeVisitStepTag(${path.getFunnel.getName})"
          case FunnelStepFieldPath.Field.TIME =>
            s"routeVisitStepTime(${path.getFunnel.getName})"
          case FunnelStepFieldPath.Field.ORDINAL=>
            s"routeVisitStepOrdinal(${path.getFunnel.getName})"
          case FunnelStepFieldPath.Field.ISFIRST=>
            s"routeVisitStepIsFirst(${path.getFunnel.getName})"
          case FunnelStepFieldPath.Field.ISLAST=>
            s"routeVisitStepIsLastInPath(${path.getFunnel.getName})"
          case FunnelStepFieldPath.Field.ISCOMPLETE=>
            s"routeVisitPathIsComplete(${path.getFunnel.getName})"
        }
        case path: FunnelStepsPath =>
          throw new IllegalStateException(s"cannot generate path ${path.getPathAsString}")
      }
}
