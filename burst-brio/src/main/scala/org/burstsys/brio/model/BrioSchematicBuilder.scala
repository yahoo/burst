/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model

import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.encoding.{BrioSchematic, BrioSchematicContext}
import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.brio.types.BrioTypes.{BrioTypeKey, BrioVersionKey}

import scala.collection.mutable

trait BrioSchematicBuilder extends Any {

  /**
    * start at root relation and build a complete DFS set of path (transitive closure) relationships
    *
    * @param schema
    */
  final
  def buildSchematics(schema: BrioSchemaContext): Unit = {
    val result = new mutable.HashMap[BrioVersionKey, mutable.HashMap[BrioTypeKey, BrioSchematic]]

    for (version <- 0 to schema.versionCount) {
      result += version -> new mutable.HashMap[BrioTypeKey, BrioSchematic]
    }
    schema.typeNameToStructureMap foreach {
      case (typeName, structure) =>
        for (version <- 0 to schema.versionCount) {
          val schematic = if (structure.validVersionSet.contains(version))
            BrioSchematic(schema, structure, version)
          else null
          result(version) += structure.structureTypeKey -> schematic

        }
    }

    /**
      * this is idiomatic scala I think, but have to admit its a bit byzantine
      */
    schema.schematicStructureVersionTypeKeyLookup = result.toList.sortBy(_._1).map {
      case (version, structureMap) => structureMap.toList.sortBy(_._1).map {
        case (structureType, schematic) => schematic
      }.toArray
    }.toArray

    schema.relationStructureVersionTypeKeyLookup = result.toList.sortBy(_._1).map {
      case (version, structureMap) => structureMap.toList.sortBy(_._1).map {
        case (structureType, null) =>
          new Array[BrioRelation](0)
        case (structureType, schematic) =>
          schematic.relationArray.map(r => r)
      }.toArray
    }.toArray
  }

}
