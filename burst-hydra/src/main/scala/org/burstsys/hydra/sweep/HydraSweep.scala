/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.sweep

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.felt.FeltService
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.hydra.{HydraReporter, parser}

/**
 * specialized sweep for the Hydra FELT language _binding. When creating a FELT language you need to implement your
 * own version of [[FeltSweep]] and add collectors and specialized inputs etc.
 */
abstract class HydraSweep extends FeltSweep {
  override def feltBinding: FeltBinding = HydraFeltBinding
}

object HydraSweep {

  /**
   * parse/validate/transform/code generate a hydra source string for a specific brio schema
   *
   * @param source
   * @param schemaName
   * @return
   */
  def apply(source: String, schemaName: BrioSchemaName): FeltSweep = {
    val brioSchema = BrioSchema(schemaName)
    var start = System.nanoTime()
    // first parse source string into an appropriate analysis felt tree
    val analysis = parser.parse(source, brioSchema)
    HydraReporter.recordParse(System.nanoTime - start, source)
    start = System.nanoTime()
    // then turn that felt tree into a cached bytecode sweep closure
    FeltService.generateSweep(source = source, analysis = analysis, brioSchema = brioSchema, binding = HydraFeltBinding)
  }

}
