/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference.relation

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * code generator for a [[FeltReference]]  to a value vector Brio reference
 */
trait FeltBrioValVecRef extends Any {

  self: FeltBrioStdRef =>

  protected
  def generateValVecRefRead(implicit cursor: FeltCodeCursor): FeltCode = {
    /*
        s"""$I// $tagName-VAL-VEC-RD  [${refName.fullPathAsStringNoQuotes}:${feltType.valueTypeAsScala}]
           |${I}if ($parentInstanceIsNull) { ${cursor.callScope.scopeNull} = true; } else {
           |${I2}val instance = $parentInstance; val schematic = $brioSchemaSym.schematic(${parentStructure.structureTypeKey}, instance.versionKey($blobReaderSym));
           |${I2}if( instance.relationIsNull($blobReaderSym, schematic, $relationOrdinal) ) { ${cursor.callScope.scopeNull} = true;  } else { ${cursor.callScope.scopeVal} = $access; }
           |$I}""".stripMargin.trimAtEnd
    */

    s"""
       | // generateValVecRefRead not implemented yet
       | ???
       | // FELT-BRIO-VAL-VEC""".stripMargin
  }


}
