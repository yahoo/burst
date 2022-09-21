/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.brio.model.parser.BrioSchemaParser
import org.burstsys.brio.model.parser.BrioSchemaParser.BrioSchemaClause
import org.burstsys.brio.model.schema.types.{BrioRelation, BrioRelationContext}
import org.burstsys.brio.types.BrioTypes.{BrioSchemaName, FirstStructureType}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.file.extractTextFilesFromClasspath
import org.burstsys.vitals.logging.VitalsLogger

import scala.jdk.CollectionConverters._

package object schema extends VitalsLogger with BrioTypeBuilder with BrioPathBuilder with BrioSchematicBuilder {

  final val rootPathKey = 1 // we are 1 based i.e. there is no '0' path

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this] val schemaMap = new ConcurrentHashMap[BrioSchemaName, BrioSchema] // don't over scala it!!

  final
  def schemaAliasSet: String = schemaMap.asScala.keys.mkString("{'", "', '", "'}")

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * get a schema by name (can be an alias
   * @param name
   * @return
   */
  final
  def getSchema(name: BrioSchemaName): BrioSchema = {
    schemaMap.get(name) match {
      case null => throw VitalsException(s"schema '$name' not found in set $schemaAliasSet")
      case schema => schema
    }
  }

  /**
   * test to see if a schema has a specific alias binding
   * @param schema
   * @param alias
   * @return
   */
  final
  def testAlias(schema:BrioSchema, alias: BrioSchemaName): Boolean = {
    val test = schemaMap.get(alias)
    test != null && test == schema
  }

  /**
   * register a schema that is built from schema version files located in a classpath location
   *
   * @param clazz                  a class that has the correct classpath for finding resources
   * @param schemaVersionClassPath the classpath that contains the '.brio' schema version files
   * @param aliases                the set of one or more names that can be used to access the resulting schema model (case sensitive)
   * @return
   */
  final def registerBrioSchema(clazz: Class[_], schemaVersionClassPath: String, aliases: BrioSchemaName*): BrioSchema = {
    // make sure none of the aliases match any of the aliases previously registered...
    if (schemaMap.asScala.keys.exists(aliases.contains))
      throw VitalsException(s"duplicate schema alias found in set $schemaAliasSet")

    // go ahead and load
    loadSchema(clazz, schemaVersionClassPath, aliases)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def loadSchema(clazz: Class[_], path: String, schemaNames: Seq[BrioSchemaName]): BrioSchema = {
    val schemata = schemaNames.mkString(", ")
    val sources = extractTextFilesFromClasspath(clazz, path, "brio")
    sources foreach {
      source =>
        if (source == null | source.isEmpty)
          throw VitalsException(s"names='$schemata', path='$path' : empty source!")
    }
    if (sources.isEmpty)
      throw VitalsException(s"names='$schemata', path='$path' : no sources found!")
    val schema = build(sources.toIndexedSeq: _*)
    if(schemaMap.contains(schema.name))
      throw VitalsException(s"parsed schema name '${schema.name}' conflicts with existing name/alias found in set $schemaAliasSet")
    schemaMap.put(schema.name, schema)
    schemaNames.foreach(schemaMap.put(_, schema))
    schema
  }

  private def build(sources: String*): BrioSchema = {
    try {
      build(sources.map(BrioSchemaParser(log).parse).toArray)
    } catch safely {
      case t: Throwable => throw t
    }
  }

  private def build(schemaClauses: Array[BrioSchemaClause]): BrioSchema = {
    // first version (version zero) is a start point for some migration invariant schema properties
    val s0 = schemaClauses.head

    val schema = BrioSchemaContext(s0.name, schemaClauses.length, s0.root.rootFieldName, s0.root.rootTypeName)
    try {

      schema._firstStructureType = FirstStructureType + 1

      /**
       * take parsed clauses and assemble into usable type set
       */
      val structureVersions = assembleVersions(schema, schemaClauses)

      /**
       * uniquely identify the parsed structures
       */
      schema.typeNameToKeyMap = bindStructureTypeKeys(structureVersions)

      /**
       * and reverse mapping
       */
      schema.typeKeyToNameMap = schema.typeNameToKeyMap.map(t => t._2 -> t._1)

      /**
       * create model structures from parsed structures
       */
      schema.typeNameToStructureMap = createSpecificTypeRelations(schema, structureVersions, schema.typeNameToKeyMap)


      /**
       * wire model structures into a super type heirarchy
       */
      createSuperTypeHeirarchy(schema, structureVersions)

      /**
       * wire model structures into a sub type reverse heirarchy
       */
      createSubTypeHeirarchy(schema, schema.typeNameToStructureMap)

      // setup root relations
      schema._rootRelation =
        BrioRelation(schema, schema.typeNameToStructureMap(schema.rootStructureType).structureTypeKey)

      schema._rootRelation.asInstanceOf[BrioRelationContext].referenceStructure =
        schema.typeNameToStructureMap(schema.rootStructureType)

      /**
       * create deep types - those that collect relations from all levels of type heirarchy
       */
      exploreSubtypeRelations(schema)

      /**
       * now build traversal path structures
       */
      buildPaths(schema)

      /**
       * initialize our set of schematics
       */
      buildSchematics(schema)

      schema
    } catch safely {
      case t: Throwable =>
        throw t
    }
  }


}
