/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.symbols

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.brio.types.{BrioPrimitives, BrioTypes}

trait FeltBrioSymbols extends AnyRef {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BRIO Class Names
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val brioBlobClass = classOf[BrioBlob].getName
  final val brioSchemaClass = classOf[BrioSchema].getName
  final val brioPrimitiveClass = BrioPrimitives.getClass.getName.stripSuffix("$")

  final val brioLatticeReferenceClass = classOf[BrioLatticeReference].getName
  final val brioLatticeReferenceAnyValClass = classOf[BrioLatticeReference].getName
  final val brioSchematicClass = classOf[BrioSchematic].getName

  final val brioMutableDictionaryClass = classOf[BrioMutableDictionary].getName
  final val brioDictionaryNotFound = s"${BrioTypes.BrioDictionaryNotFound}"

}
