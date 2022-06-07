/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference.relation

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.sweep.symbols.{blobDictionarySym, blobReaderSym, brioSchemaSym, sweepRuntimeSym}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, T}

/**
 * code generator for a [[FeltReference]]  to a value scalar Brio reference
 * == TODO ==
 * <ol>
 * <li>Port this code to use the [[org.burstsys.felt.model.sweep.lexicon.FeltLexicon]] which
 * will eliminate repeated dictionary lookups of statically defined strings</li>
 * </ol>
 */
trait FeltBrioValScalRef extends Any {

  self: FeltBrioStdRef =>

  protected
  def generateValScalRefRead(implicit cursor: FeltCodeCursor): FeltCode = {

    val access = valueType match {
      case BrioBooleanKey => s"instance.valueScalarBoolean($blobReaderSym, schematic, $relationOrdinal)"
      case BrioByteKey => s"instance.valueScalarByte($blobReaderSym, schematic, $relationOrdinal)"
      case BrioShortKey => s"instance.valueScalarShort($blobReaderSym, schematic, $relationOrdinal)"
      case BrioIntegerKey => s"instance.valueScalarInteger($blobReaderSym, schematic, $relationOrdinal)"
      case BrioLongKey => s"instance.valueScalarLong($blobReaderSym, schematic, $relationOrdinal)"
      case BrioDoubleKey => s"instance.valueScalarDouble($blobReaderSym, schematic, $relationOrdinal)"
      case BrioStringKey =>
        if (global.lexicon.enabled)
          s"instance.valueScalarString($blobReaderSym, schematic, $relationOrdinal)"
        else
          s"$blobDictionarySym.stringLookup(instance.valueScalarString($blobReaderSym, schematic, $relationOrdinal))($sweepRuntimeSym.text)"
      case _ => throw FeltException(refName.location, s"${refDecl.relation.valueEncoding.typeKey} type for path $refName not supported")
    }

    s"""|
        |${T(this)}
        |${I}if ($parentInstanceIsNull) { ${cursor.callScope.scopeNull} = true; } else {
        |${I2}val instance = $parentInstance; val schematic = $brioSchemaSym.schematic(${parentStructure.structureTypeKey}, instance.versionKey($blobReaderSym));
        |${I2}if( instance.relationIsNull($blobReaderSym, schematic, $relationOrdinal) ) { ${cursor.callScope.scopeNull} = true;  } else { ${cursor.callScope.scopeVal} = $access; }
        |$I} // FELT-BRIO-VAL-SCAL""".stripMargin

  }


}
