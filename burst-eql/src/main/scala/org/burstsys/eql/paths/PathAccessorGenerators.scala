/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionSourceGenerator
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.motif.motif.tree.data.PathAccessor
import org.burstsys.motif.paths.funnels.FunnelPathBase
import org.burstsys.motif.paths.schemas.{MapLookupPath, SchemaPathBase}
import org.burstsys.motif.paths.segments.SegmentPathBase

trait PathAccessorGenerators extends Any {
  class PathAccessorSourceGenerator(pathAccessor: PathAccessor) extends ActionSourceGenerator {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
        pathAccessor.getBinding.getPath match {
          case _: MapLookupPath =>
            new SchemaPathGenerator(pathAccessor).generateSource()
          case _: SchemaPathBase =>
            new SchemaPathGenerator(pathAccessor).generateSource()
          case fp: FunnelPathBase =>
            new RoutePathGenerator(fp).generateSource()
          case sp: SegmentPathBase =>
            new SegmentPathGenerator(sp).generateSource()
          case _ =>
            throw new IllegalStateException(s"invalid path type ${pathAccessor.getBinding.getPath.getClass.getName}")
        }
      }
    }
}
