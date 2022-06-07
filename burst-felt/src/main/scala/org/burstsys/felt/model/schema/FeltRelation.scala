/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema

import org.burstsys.brio.model.schema.encoding.BrioValueEncoding
import org.burstsys.brio.model.schema.types.{BrioExtendedRelation, BrioRelation, BrioRelationForm, BrioStructure}
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioRelationOrdinal, BrioTypeKey, BrioVersionKey}

trait FeltRelation extends BrioRelation

object FeltRelation {

  def apply(relationName: BrioRelationName): FeltRelation =
    FeltRelationContext(relationName: BrioRelationName)
}

private final case
class FeltRelationContext(relationName: BrioRelationName) extends FeltRelation {
  override def relationForm: BrioRelationForm = BrioExtendedRelation

  override def parentStructure: BrioStructure = ???

  override def validTypeSet: Set[BrioTypeKey] = ???

  override def validVersionSet: Set[BrioVersionKey] = ???

  override def valueOrReferenceTypeKey: BrioTypeKey = ???

  override def relationOrdinal: BrioRelationOrdinal = ???

  override def referenceStructure: BrioStructure = ???

  override def valueEncoding: BrioValueEncoding = ???

  override def keyEncoding: BrioValueEncoding = ???

  override def isKey: Boolean = ???

  override def isOrdinal: Boolean = ???

}
