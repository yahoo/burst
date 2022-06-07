/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.hydra.utils
import org.burstsys.eql.generators.{ActionSourceGenerator, toActionGenerator}
import org.burstsys.motif.motif.tree.data.{PathAccessor, ValueMapBinding, ValueVectorBinding}
import org.burstsys.motif.paths.schemas.{MapKeyPath, MapLookupPath, MapValuePath, ValueVectorValuePath}

class SchemaPathGenerator(pathAccessor: PathAccessor) extends ActionSourceGenerator {
    override def generateSource()(implicit context: GlobalContext): utils.CodeBlock = {
      pathAccessor.getBinding match {
        case vmb: ValueMapBinding =>
          val lp = pathAccessor.getLowestEvaluationPoint
          lp match {
            case mkp: MapKeyPath =>
              assert(pathAccessor.getMapKey == null)
              s"key($mkp)"
            case mvp: MapValuePath =>
              assert(pathAccessor.getMapKey == null)
              s"value($mvp)"
            case mlp: MapLookupPath =>
              val e = vmb.getMapKeyExpression.generateSource()
              assert(e.length == 1)
              s"$mlp[${e.head}]"
            case _ =>
              s"${pathAccessor.fullPathAsString()}"
          }
        case _: ValueVectorBinding =>
          assert(pathAccessor.getMapKey == null)
          pathAccessor.getLowestEvaluationPoint match {
            case vvp: ValueVectorValuePath =>
              s"value($vvp)"
            case _ =>
              s"${pathAccessor.fullPathAsString()}"
          }
        case _ =>
          s"${pathAccessor.fullPathAsString()}"
      }
    }
}
