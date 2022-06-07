/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.model.schema.encoding.{BrioSchematic, BrioSchematicContext}
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * print brio structure (for debugging)
  *
  */
trait BrioStructurePrinter extends Any {

  self: BrioPresserContext =>

  /**
    * given a structure start offset, print out vital info on structure (for debugging)
    *
    * @param schematic
    * @param structureStartOffset
    * @return
    */
  final
  def printStructure(schematic: BrioSchematic, structureStartOffset: TeslaMemoryOffset): String = {

    /**
      * name and version
      */
    val structureName = schematic.schema.structureName(schematic.structureKey)
    val version = sink.buffer.readInteger(structureStartOffset)

    /**
      * nulls map
      */
    val nullsMap = (for (i <- 0 until schematic.relationCount) yield {
      if (!BrioNulls.relationTestNull(sink.buffer, i, structureStartOffset + SizeOfInteger)) i.toString else ""
    }).mkString("(", ", ", ")")

    /**
      * fixed scalar value relations
      */
    val fixedRelations = (for (i <- 0 until schematic.relationCount) yield {
      val form = schematic.form(i)
      if (form == BrioValueScalarRelation) {
        val relation = schematic.relationArray(i)
        val relationName = relation.relationName
        val relationType = relation.valueEncoding.typeName
        val offset = schematic.fixedRelationsOffsets(i) + structureStartOffset
        val value = relation.valueEncoding.typeKey match {
          case BrioBooleanKey => sink.buffer.readBoolean(offset).toString
          case BrioByteKey => sink.buffer.readByte(offset).toString
          case BrioShortKey => sink.buffer.readShort(offset).toString
          case BrioIntegerKey => sink.buffer.readInteger(offset).toString
          case BrioLongKey => sink.buffer.readLong(offset).toString
          case BrioDoubleKey => sink.buffer.readDouble(offset).toString
          case BrioStringKey => sink.buffer.readShort(offset).toString
          case _ => throw VitalsException(s"${relation.valueEncoding.typeKey} not matched")
        }
        s"\t\t$i) '$relationName' : $relationType : $value : offset($offset)\n"
      } else ""
    }).mkString("")


    /**
      * variable size offset table
      */
    val offsetTable = (for (i <- 0 until schematic.relationCount) yield {
      val form = schematic.form(i)
      if (form != BrioValueScalarRelation) {
        val variableRelationKey = schematic.variableRelationOffsetKeys(i)
        val index = structureStartOffset + schematic.variableRelationOffsetsStart + (variableRelationKey * SizeOfOffset)
        val offset = sink.buffer.readOffset(index)
        val relation = schematic.relationArray(i)
        val relationName = relation.relationName
        val extras = relation.relationForm match {
          case BrioValueVectorRelation =>
            val members = sink.buffer.readShort(offset + structureStartOffset)
            s" : members=$members"
          case BrioValueMapRelation =>
            val entries = sink.buffer.readShort(offset + structureStartOffset)
            s" : entries=$entries"
          case BrioReferenceScalarRelation => s""
          case BrioReferenceVectorRelation =>
            val members = sink.buffer.readShort(offset + structureStartOffset)
            val firstMemberSize = sink.buffer.readShort(offset + structureStartOffset + SizeOfShort)
            s" : members=$members, firstMemberSize=$firstMemberSize"
        }
        s"\t\t$i) '$relationName': $form : offset($index)=$offset$extras\n"
      } else ""
    }).mkString("")

    /**
      * final print out
      */
    s"""
       |$structureName(
       |    offset=$structureStartOffset
       |    version=$version
       |    ordinals $nullsMap are non null
       |
       |    fixed size relations:
       |$fixedRelations
       |    variable size relations:
       |$offsetTable
       |)
     """.stripMargin
  }

}
