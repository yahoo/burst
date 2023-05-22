/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.model.schema.types.BrioStructure
import org.burstsys.brio.model.schema.{BrioSchema, BrioSchemaContext}
import org.burstsys.brio.press.roller._
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.annotation.tailrec


/**
 * API for presser
 */
trait BrioPresser extends Any {

  /**
   * Schema driven event handling roll forward press of data into a brio compatible output data format including
   * associated brio dictionary for strings, and offset/lookup tables for extended types.
   */
  def press(schema: BrioSchema, source: BrioPressSource): BrioPressSink

}

object BrioPresser {
  def apply[I <: BrioPressInstance](sink: BrioPressSink): BrioPresser =
    BrioPresserContext(sink)
}

/**
 * * @param schema
 * @param sink
 * @param source
 */
private[press] final case
class BrioPresserContext(sink: BrioPressSink)
  extends BrioPresser with BrioValueScalarRoller with BrioValueMapRoller with BrioValueVectorRoller
    with BrioStructureRoller with BrioReferenceScalarRoller with BrioReferenceVectorRoller
    with BrioStructurePrinter {

  protected val capture: BrioPressCaptureContext = BrioPressCaptureContext(sink.dictionary)

  protected implicit val text: VitalsTextCodec = VitalsTextCodec()

  protected var source: BrioPressSource = _

  protected var schemaContext: BrioSchemaContext = _

  protected var cursor: BrioPressCursorContext = _

  protected var rollOffset: TeslaMemoryOffset = 0

  override def press(schema: BrioSchema, toPress: BrioPressSource): BrioPressSink = {
    source = toPress
    schemaContext = schema.asInstanceOf[BrioSchemaContext]
    cursor = BrioPressCursorContext(schema.rootRelationName, schema.keyForPath(schema.rootRelationName), schema.rootRelationName, 0)
    rollOffset = 0
    try {
      if (log.isDebugEnabled)
        log debug burstStdMsg(s"${schemaContext.rootRelation.relationName}:${schemaContext.rootRelation.relationForm}")
      rollStructure(source.extractRootReferenceScalar(), schemaContext.rootNode)
      sink.buffer.currentUsedMemory(rollOffset)
      sink
    } catch safely { // TODO: catch Size Overflow
      case t: Throwable =>
        log error(burstStdMsg(t), t)
        throw t
    }

  }

}
