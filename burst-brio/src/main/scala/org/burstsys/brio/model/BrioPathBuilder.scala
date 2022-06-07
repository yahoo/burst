/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model

import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioRelationContext, BrioStructure}
import org.burstsys.brio.types.BrioPath._

import scala.collection.JavaConverters._

trait BrioPathBuilder extends Any {

  /**
   * start at root relation and build a complete DFS set of path (transitive closure) relationships
   *
   * @param schema
   */
  final
  def buildPaths(schema: BrioSchemaContext): Unit = {

    /**
     * set up root relation
     */
    val rootRelationContext = schema._rootRelation.asInstanceOf[BrioRelationContext]
    rootRelationContext.relationPathName = schema.rootRelationName
    rootRelationContext.relationPathKey = 1

    addPathRelationSlot(schema, rootRelationContext, schema.rootRelationName, RootParentPathKey)

    /**
     * build hierarchy
     */
    buildPathsFromNormalizedRelations(schema, schema.rootRelationName, rootRelationContext.relationPathKey,
      schema._rootRelation.referenceStructure)

    /**
     * collect aggregate context
     */
    schema._parentPathKeys = new Array(schema.pathCount + 1)
    schema.parentPathKeyToKeyMap.asScala.toList.sortBy(_._1) foreach {
      case (key, parentKey) => schema._parentPathKeys(key - 1) = parentKey
    }
  }

  private
  def addPathRelationSlot(schema: BrioSchemaContext, relation: BrioRelationContext, pathName: BrioPathName,
                          parentPathKey: BrioPathKey): Unit = {
    relation.relationPathName = pathName
    relation.relationPathKey = schema.pathIndex.getAndIncrement
    val relationCopy = relation.duplicate
    schema.parentPathKeyToKeyMap.addValue(relationCopy.relationPathKey, parentPathKey)
    schema.pathNameToKeyMap.addValue(relationCopy.relationPathName, relationCopy.relationPathKey)
  }

  /**
   * recursive routine to build paths as part of a DFS traversal
   *
   * @param schema
   * @param pathPrefix
   * @param structure
   */
  private
  def buildPathsFromNormalizedRelations(schema: BrioSchemaContext, pathPrefix: String,
                                        parentPathKey: BrioPathKey, structure: BrioStructure): Unit = {
    structure.normalizedStructureRelationNameMap.values.toList.sortBy(_.relationOrdinal) foreach {
      case relation: BrioRelationContext =>
        val newPathName = pathPrefix + '.' + relation.relationName
        addPathRelationSlot(schema, relation, newPathName, parentPathKey)
        relation.relationForm match {
          case BrioReferenceScalarRelation | BrioReferenceVectorRelation =>
            buildPathsFromNormalizedRelations(schema, newPathName, relation.relationPathKey,
              relation.referenceStructure)
          case _ =>
        }
    }
  }

}
