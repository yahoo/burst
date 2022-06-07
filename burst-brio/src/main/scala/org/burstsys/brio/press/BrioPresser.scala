/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.model.schema.types.BrioStructure
import org.burstsys.brio.model.schema.{BrioSchema, BrioSchemaContext}
import org.burstsys.brio.press.roller._
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._


/**
  * API for presser
  */
trait BrioPresser extends Any {

  /**
    * Schema driven event handling roll forward press of data into a brio compatible output data format including
    * associated brio dictionary for strings, and offset/lookup tables for extended types.
    *
    * @return
    */
  def press: BrioPressSink

}

object BrioPresser {
  def apply[I <: BrioPressInstance](schema: BrioSchema, sink: BrioPressSink, source: BrioPressSource): BrioPresser =
    BrioPresserContext(schema, sink, source)
}

/**
  *
  * @param schema
  * @param sink
  * @param source
  */
private[press] final case
class BrioPresserContext(schema: BrioSchema, sink: BrioPressSink, source: BrioPressSource)
  extends BrioPresser with BrioValueScalarRoller with BrioValueMapRoller with BrioValueVectorRoller
    with BrioStructureRoller with BrioReferenceScalarRoller with BrioReferenceVectorRoller
    with BrioStructurePrinter {

  protected
  val schemaContext: BrioSchemaContext = schema.asInstanceOf[BrioSchemaContext]

  protected
  val capture: BrioPressCaptureContext = BrioPressCaptureContext(sink.dictionary)

  protected
  val cursor: BrioPressCursorContext = BrioPressCursorContext(schema.rootRelationName, schema.keyForPath(schema.rootRelationName), schema.rootRelationName, 0)

  protected
  val rootStructure: BrioStructure = schemaContext._rootRelation.referenceStructure

  protected
  var rollOffset: TeslaMemoryOffset = 0

  protected
  implicit val text: VitalsTextCodec = VitalsTextCodec()

  def press: BrioPressSink = {
    try {
      //      log debug burstStdMsg(s"${schemaContext.rootRelation.relationPathName}:${schemaContext.rootRelation.relationForm}")
      rollStructure(source.extractRootReferenceScalar(), schemaContext.rootNode)
      sink.buffer.currentUsedMemory(rollOffset)
      sink
    } catch safely { // TODO: catch Size Overflow
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }


}
