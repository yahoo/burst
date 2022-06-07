/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.schemas.SchemaPathBase
import org.burstsys.motif.paths.segments.{SegmentMembersPath, SegmentPathBase}

class SegmentVisitPath(motifPath: SegmentPathBase) extends DynamicVisitPath {
  override def getPathAsString: String = motifPath.getPathAsString

  override def getEnclosingStructure: Path = motifPath.getEnclosingStructure

  override def getParentStructure: Path = motifPath.getParentStructure

  protected def sameOnPath(p: Path): Boolean = motifPath.sameOnPath(p)

  protected def sameHigher(p: Path): Boolean = motifPath.sameHigher(p)

  protected def sameLower(p: Path): Boolean = motifPath.sameLower(p)

  override def getNavigatorId: String = motifPath.getSegment.getName

  override def walkPaths[B <: AnyRef]
  (input: Option[B],
   preAction: Option[(VisitPath, Option[B]) => Option[B]],
   postAction: Option[(VisitPath, List[B]) => Option[B]],
   dynamicPaths:  Option[VisitPathLookup] = None
  ): Option[B] = {
    // do the pre-action
    val preResult = if (preAction.isDefined) preAction.get(this, input) else None

    // walk the static relations
    var childrenResults:  Iterator[Option[B]] = Iterator(this.motifPath match {
      case _: SegmentMembersPath =>
        None
      case _ =>
        throw new IllegalStateException(s"invalid segment path type ${this.motifPath.getClass.getName}")
    })

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


  override def isRoot: Boolean = motifPath.isRoot

  override def toString: String = getPathAsString

  override def hashCode(): Int = getPathAsString.hashCode()

  override def equals(obj: Any): Boolean = obj match {
    case fvp: SegmentVisitPath =>
      getPathAsString.equals(fvp.getPathAsString)
    case _ => false
  }

  override def notOnPath(p: Path): Boolean = motifPath.notOnPath(p)

  override def higher(p: Path): Boolean = motifPath.higher(p)

  override def lower(p: Path): Boolean = motifPath.lower(p)

  override val getAttachmentPath: VisitPath =
    VisitPath(SchemaPathBase.formPath(motifPath.getSegment.getSchema, motifPath.getSegment.getSchema.getRootFieldName, null))

  override def getLocalPath: VisitPath = this

  override def getLocalRoot: VisitPath = this
}

