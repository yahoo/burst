/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.funnels.FunnelPathBase
import org.burstsys.motif.paths.schemas.{MapLookupPath, SchemaPathBase}
import org.burstsys.motif.paths.segments.SegmentPathBase

import scala.language.implicitConversions

package object paths {
  object VisitPath {
    def apply(motifPath: Path): VisitPath = {
      motifPath match {
        case vp: VisitPath =>
          vp
        case mlp: MapLookupPath =>
          new SchemaVisitPath(mlp.getEnclosingStructure)
        case sp: SchemaPathBase =>
          new SchemaVisitPath(sp)
        case fp: FunnelPathBase =>
          new FunnelVisitPath(fp)
        case sp: SegmentPathBase =>
          new SegmentVisitPath(sp)
        case _ =>
          throw new IllegalStateException(s"invalid path type ${motifPath.getClass.getName}")
      }
    }
  }

  type VisitPathLookup = Map[VisitPath, List[VisitPath]]

  trait VisitPath extends Path {
    def getNavigatorId: String

    def walkPaths[B <: AnyRef]
    (input: Option[B],
     preAction: Option[(VisitPath, Option[B]) => Option[B]],
     postAction: Option[(VisitPath, List[B]) => Option[B]],
     dynamicPaths: Option[VisitPathLookup] = None
    ): Option[B]
  }

  trait DynamicVisitPath extends VisitPath {
    def getAttachmentPath:  VisitPath
    def getLocalPath: VisitPath
    def getLocalRoot: VisitPath
  }
}
