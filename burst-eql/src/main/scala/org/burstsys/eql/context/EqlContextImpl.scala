/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.context

import org.burstsys.eql._
import org.burstsys.eql.generators.hydra.routes.Route
import org.burstsys.eql.generators.hydra.tablets.Tablet
import org.burstsys.eql.generators.{BlockGenerator, DeclarationScope, SchemaDeclaration}
import org.burstsys.eql.parsing.{ParsedBlock, ParsedFunnel, ParsedQuery, ParsedSegment}
import org.burstsys.eql.planning.funnels.Funnel
import org.burstsys.eql.planning.queries.Query
import org.burstsys.eql.planning.segments.Segment
import org.burstsys.eql.trek.{EqlSupervisorQueryGenerate, EqlSupervisorQueryParse, EqlSupervisorQueryPlan}
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupUid
import org.burstsys.vitals.errors.VitalsException

import scala.language.postfixOps

class EqlContextImpl(val guid: FabricGroupUid) extends EqlContext {

  override lazy implicit val globalContext: GlobalContext = new GlobalContext()

  /**
   * Compile an EQL query into Hydra code.
   * @param schemaName optionally provide a schema name which will be validated against the query schema
   * @param source the EQL source
   * @return
   */
  override def eqlToHydra(schemaName: Option[String], source: String): String = {
    // reset the context so generation is identical
    globalContext.reset

    // build the parse tree
    val sqplans = EqlSupervisorQueryParse.begin(guid)
    val block = ParsedBlock(source)
    EqlSupervisorQueryParse.end(sqplans)

    val executions = block.getStatements.flatMap {
      case query: ParsedQuery =>

        // analyze the query for work
        val sqps = EqlSupervisorQueryPlan.begin(guid)
        val analysis = Query(query)
        EqlSupervisorQueryPlan.end(sqps)

        // add a declaration for the primary schema in the global context
        val thisSchemaDeclaration = SchemaDeclaration(DeclarationScope.Frame, query.getSchema)
        globalContext.addDeclaration(thisSchemaDeclaration.name.toLowerCase, thisSchemaDeclaration)

        // add aliases for sources
        for (s <- analysis.getSources) {
          val decl = globalContext.getDeclaration(s.declaredName.toLowerCase)
          decl match {
            case schema: SchemaDeclaration =>
              globalContext.addDeclaration(s.name, schema)
            case route: Route =>
              globalContext.addDeclaration(s.name, route)
            case tablet: Tablet =>
              globalContext.addDeclaration(s.name, tablet)
          }
        }

        // generate hydra
        val sqgs = EqlSupervisorQueryGenerate.begin(guid)
        val generator = BlockGenerator(analysis)

        val querySchema = query.getSchema.getSchemaName.toLowerCase.trim
        if (schemaName.isDefined && schemaName.get.toLowerCase.trim != querySchema) {
          throw VitalsException(s"target schema '$schemaName' does not match the EQL declared schema '$querySchema'")
        }
        val source = generator.generateSource()
        EqlSupervisorQueryGenerate.end(sqgs)

        Some(source)
      case funnel: ParsedFunnel =>
        val analysis = Funnel(funnel)
        // add a declaration for the primary schema in the global context
        val thisSchemaDeclaration = SchemaDeclaration(DeclarationScope.Frame, funnel.getSchema)
        globalContext.addDeclaration(thisSchemaDeclaration.name, thisSchemaDeclaration)
        globalContext.addDeclaration(thisSchemaDeclaration.schema.getRootFieldName, thisSchemaDeclaration)
        val route = new Route(analysis)
        globalContext.addDeclaration(route.name.toLowerCase, route)
        None
      case segment: ParsedSegment =>
        val analysis = Segment(segment)
        val thisSchemaDeclaration = SchemaDeclaration(DeclarationScope.Frame, segment.getSchema)
        globalContext.addDeclaration(thisSchemaDeclaration.name, thisSchemaDeclaration)
        globalContext.addDeclaration(thisSchemaDeclaration.schema.getRootFieldName, thisSchemaDeclaration)
        val tablet = new Tablet(analysis)
        globalContext.addDeclaration(tablet.name.toLowerCase, tablet)
        None
    }

    //TODO return multiple executions for a true batch
    assert(executions.length == 1)
    executions.head

  }

}

