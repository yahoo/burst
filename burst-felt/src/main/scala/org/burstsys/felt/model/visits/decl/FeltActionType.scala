/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.decl

import org.burstsys.brio.model.schema.types._
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, _}

/**
 * The type of the Action within a Visit within a Query within an Analysis
 */
sealed case class FeltActionType(name: String, placement: FeltPlacement, validRelationForms: BrioRelationForm*) {
  final override
  def toString: String = name
}

/**
 * defines an action that get executed ''before'' any child-relations are explored
 */
object FeltPreActionType extends FeltActionType("pre", FeltInstancePrePlace,
  BrioReferenceScalarRelation,
  BrioReferenceVectorRelation
)

/**
 * defines an action that get executed ''after'' any child-relations are explored
 */
object FeltPostActionType extends FeltActionType("post", FeltInstancePostPlace,
  BrioReferenceScalarRelation,
  BrioReferenceVectorRelation
)

/**
 * defines an action that get executed ''as'' each member in a collection child-relation is explored
 */
object FeltSituActionType extends FeltActionType("situ", FeltVectorMemberSituPlace,
  BrioValueVectorRelation,
  BrioValueMapRelation
)

/**
 * defines an action that get executed ''before'' all members in a collection child-relation are explored
 */
object FeltBeforeActionType extends FeltActionType("before", FeltVectorBeforePlace,
  BrioReferenceVectorRelation,
  BrioValueVectorRelation,
  BrioValueMapRelation
)

/**
 * defines an action that get executed ''after'' all members in a collection child-relation are explored
 */
object FeltAfterActionType extends FeltActionType("after", FeltVectorAfterPlace,
  BrioReferenceVectorRelation,
  BrioValueVectorRelation,
  BrioValueMapRelation
)
