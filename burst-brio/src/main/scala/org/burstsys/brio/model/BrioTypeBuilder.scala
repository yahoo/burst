/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model

import org.burstsys.brio.model.parser.BrioSchemaParser.{BrioSchemaClause, BrioSchemaOrdinal, BrioSchemaStructureClause}
import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes.{BrioTypeKey, BrioTypeName, FirstStructureType}
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait BrioTypeBuilder extends Any {

  /**
   *  go through the schema and the constructed structures and build a super type hierarchy
   * @param schema
   * @param structureVersions
   */
  final
  def createSuperTypeHeirarchy(schema: BrioSchemaContext, structureVersions: Map[String, Array[BrioSchemaStructureClause]]): Unit = {
    structureVersions foreach {
      case (structureName, versionClauses) =>
        val superStructure = versionClauses.head.superTypeName match {
          case null => null
          case name => schema.typeNameToStructureMap(name)
        }
        schema.typeNameToStructureMap(structureName).asInstanceOf[BrioStructureContext].superStructure = superStructure
    }
  }

  /**
   *  go through the schema and the constructed structures and build a super type hierarchy
   * @param schema
   * @param structureMap
   */
  final
  def createSubTypeHeirarchy(schema: BrioSchemaContext, structureMap: Map[BrioTypeName, BrioStructure]): Unit = {
    structureMap.foreach {
      case (typeName, structure: BrioStructureContext) =>
        structure.subStructures =
          structureMap.values.filter {
            s => s.superStructure != null && s.superStructure == structure
          }.toArray

        structure.specificStructureRelationNameMap.values foreach {
          case relation: BrioRelationContext => relation.relationForm match {
            case BrioReferenceScalarRelation | BrioReferenceVectorRelation =>
              relation.referenceStructure = schema.typeNameToStructureMap(relation.referenceTypeName)
            case _ =>
          }
        }

      case (_, _) => ??? // SHOULD NOT HAPPEN
    }
  }

  /**
   * create structure name -> structure definition that are a validated merge of all versions of a structure...
   *
   * @param schema
   * @param structureVersions
   * @param typeKeyMap
   * @return
   */
  final
  def createSpecificTypeRelations(schema: BrioSchemaContext, structureVersions: Map[String, Array[BrioSchemaStructureClause]],
                                  typeKeyMap: Map[BrioTypeName, BrioTypeKey]): Map[BrioTypeName, BrioStructure] = {
    val result = new mutable.HashMap[BrioTypeName, BrioStructure]
    structureVersions foreach {
      case (structureName, versionClauses) =>
        val relationNames = new mutable.HashMap[BrioTypeName, BrioRelation]
        val relationOrdinals = new mutable.HashMap[BrioSchemaOrdinal, BrioRelation]
        val structureTypeKey = typeKeyMap(structureName)
        val structure = BrioStructure(schema, structureName, structureTypeKey).asInstanceOf[BrioStructureContext]
        result += structureName -> structure
        versionClauses foreach {
          version =>
            if (version.superTypeName != versionClauses.head.superTypeName)
              throw VitalsException(s"structure '$structureName' illegal supertyping schema evolution")
        }
        versionClauses.sortBy(_.version) foreach {
          structureClause =>
            structureClause.relations foreach {
              relationClause =>
                relationOrdinals get relationClause.relationOrdinal match {
                  case None =>
                    val relation = BrioRelation(schema, structure, relationClause).asInstanceOf[BrioRelationContext]
                    relation.validVersionSet = Set(structureClause.version)
                    relationNames += relationClause.relationName -> relation
                    relationOrdinals += relationClause.relationOrdinal -> relation
                  case Some(r: BrioRelationContext) =>
                    if (!BrioRelation(schema, structure, relationClause).equals(r))
                      throw VitalsException(s"structureName='$structureName', version=${
                        structureClause.version
                      }, relationName='${r.relationName}' doesn't match")
                    r.validVersionSet ++= Set(structureClause.version)
                  case _ => ??? // SHOULD NOT HAPPEN
                }
            }
        }
        structure.specificStructureRelationNameMap = relationNames.toMap
        structure.specificStructureRelationOrdinalMap = relationOrdinals.toMap
        structure.validVersionSet = versionClauses.map(_.version).toSet
    }
    result.toMap
  }

  /**
   * go through schema and process all subtype relationships
   * @param schema
   */
  final
  def exploreSubtypeRelations(schema: BrioSchemaContext): Unit = {
    schema.typeNameToStructureMap foreach {
      case (typeName, structure: BrioStructureContext) =>

        val specificRelations = structure.specificStructureRelationNameMap

        // collect transitive relations
        val transitiveRelations = specificRelations ++ structure.collectSuperRelations

        val normalizedRelations = transitiveRelations ++ structure.root.collectSubRelations

        // update schema
        structure.transitiveStructureRelationNameMap = transitiveRelations
        structure.transitiveStructureRelationOrdinalMap = transitiveRelations.map(r => r._2.relationOrdinal -> r._2)
        structure.normalizedStructureRelationNameMap = normalizedRelations
        structure.normalizedStructureRelationOrdinalMap = normalizedRelations.map(r => r._2.relationOrdinal -> r._2)

      case (_, _) => ??? // SHOULD NOT HAPPEN
    }
  }

  /**
   * create and validate a set of structure names -> structure id mappings
   *
   * @param versionStructMap
   * @return
   */
  final
  def bindStructureTypeKeys(versionStructMap: Map[String, Array[BrioSchemaStructureClause]]): Map[BrioTypeName, BrioTypeKey] = {
    val result = new mutable.HashMap[BrioTypeName, BrioTypeKey]
    var structureId: BrioTypeKey = FirstStructureType
    versionStructMap.toList.sortBy(_._1).toMap foreach {
      case (structName, structureList) =>
        structureId += 1
        result += structName -> structureId
        structureId
    }
    result.toMap
  }

  /**
   * create and validate a set of structure name -> structure definition version(s) mappings
   *
   * @param schema
   * @param schemaClauses
   * @return
   */
  final
  def assembleVersions(schema: BrioSchemaContext, schemaClauses: Array[BrioSchemaClause]): Map[String, Array[BrioSchemaStructureClause]] = {
    val result = new mutable.HashMap[String, ArrayBuffer[BrioSchemaStructureClause]]
    var v = 1
    schemaClauses sortBy (_.version) foreach {
      schemaClause =>
        if (v == 0) throw VitalsException(s"no version zero allowed in schema set...")
        if (v == 1 && schemaClause.version != 1) throw VitalsException(s"no version one in schema set...")
        else if (schemaClause.version != v) throw VitalsException(s"version gap in schema set...")
        v += 1
        if (schemaClause.name.toLowerCase != schema.name.toLowerCase)
          throw VitalsException(s"mismatched names ('${schemaClause.name.toLowerCase}', '${schema.name.toLowerCase}') in schema set...")
        schemaClause.structures foreach {
          structure =>
            result getOrElseUpdate(
              structure.selfName,
              new ArrayBuffer[BrioSchemaStructureClause]
            ) += structure
        }
    }
    result.map(v => v._1 -> v._2.toArray).toMap
  }

}
