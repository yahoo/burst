/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.schemas.SchemaPathBase
import org.burstsys.motif.schema.model.{SchemaReference, SchemaValueMap, SchemaValueVector}
import org.burstsys.vitals.errors.VitalsException

import scala.jdk.CollectionConverters._

class SchemaVisitPath(motifPath: SchemaPathBase) extends  VisitPath {
  override def getPathAsString: String = motifPath.getPathAsString

  override def getEnclosingStructure: Path = motifPath.getEnclosingStructure

  override def getParentStructure: Path = motifPath.getParentStructure

  protected def sameOnPath(p: Path): Boolean = motifPath.sameOnPath(p)

  protected def sameHigher(p: Path): Boolean = motifPath.sameHigher(p)

  protected def sameLower(p: Path): Boolean = motifPath.sameLower(p)

  /**
   * Using the schema, do a DFS walk all the paths that are a reference or a collection.
   *
   * @param postAction code to invoke at each node *after* all the children have been walked and their
   *               actions processes
   * @tparam B type of object returned by the action
   * @return An optional object from the last action
   */
  def walkPaths[B <: AnyRef]
  (input: Option[B],
    preAction: Option[(VisitPath, Option[B]) => Option[B]],
    postAction: Option[(VisitPath, List[B]) => Option[B]],
    dynamicPaths:  Option[VisitPathLookup] = None
  ): Option[B]= {
    // look it up
    val struct = motifPath.getSchema.getStructurePathMap.asScala(getPathAsString)
    if (struct == null)
      throw VitalsException(s"path '$this' does not refer to a structure in schema '${motifPath.getSchema.getSchemaName}")

    // do the pre-action
    val preResult = if (preAction.isDefined) preAction.get(this, input) else None

    // walk the static relations
    val targetStructure = struct.getReferenceType
    var childrenResults = for (r <- targetStructure.getRelationNameMap.values.asScala) yield
      r match {
        case ref: SchemaReference =>
          val childPath = VisitPath(SchemaPathBase.formPath(motifPath.getSchema, s"$this.${ref.getFieldName}", null))
          childPath.walkPaths(preResult, preAction, postAction, dynamicPaths)
        case map: SchemaValueMap =>
          val childPath = VisitPath(SchemaPathBase.formPath(motifPath.getSchema, s"$this.${map.getFieldName}", null))
          if (preAction.isDefined) preAction.get(childPath, preResult)
          if (postAction.isDefined) postAction.get(childPath, List()) else None
        case vv: SchemaValueVector =>
          val childPath = VisitPath(SchemaPathBase.formPath(motifPath.getSchema, s"$this.${vv.getFieldName}", null))
          if (preAction.isDefined) preAction.get(childPath, preResult)
          if (postAction.isDefined) postAction.get(childPath, List()) else None
        case _ => // ignore other scalars
          None
      }

    // do the dynamic relations
    if (dynamicPaths.isDefined && dynamicPaths.get.contains(this)) {
      val dynamicChildren = dynamicPaths.get(this)
      val dynamicChildrenResults = for (r <- dynamicChildren) yield {
        r.walkPaths(preResult, preAction, postAction, dynamicPaths)
      }
      childrenResults ++= dynamicChildrenResults
    }

    // do the action for this structure
    if (postAction.isDefined)
      postAction.get(this, childrenResults.flatten.toList)
    else
      None

  }

  override def getNavigatorId: String = SchemaPathBase.rootPath(motifPath.getSchema).getPathAsString

  override def isRoot: Boolean = motifPath.isRoot

  override def toString: String = motifPath.getPathAsString

  override def hashCode(): Int = motifPath.getPathAsString.hashCode()

  override def equals(obj: Any): Boolean = obj match {
    case svp: SchemaVisitPath =>
      motifPath.getPathAsString.equals(svp.getPathAsString)
    case _ => false
  }

  override def notOnPath(p: Path): Boolean = motifPath.notOnPath(p)

  override def higher(p: Path): Boolean = motifPath.higher(p)

  override def lower(p: Path): Boolean = motifPath.lower(p)

}

