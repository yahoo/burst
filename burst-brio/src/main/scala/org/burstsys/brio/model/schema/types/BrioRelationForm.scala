/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.types

/**
  * Each [[BrioRelation]] is one of a small set of well defined relationship forms, each with
  * unique object modeling semantics
  */
trait BrioRelationForm extends Any {

  /**
    * a unique code for this form
    *
    * @return
    */
  def code: Int

  /**
    * the name for this form
    *
    * @return
    */
  def name: String

  /**
    * is this form of relation a value?
    *
    * @return
    */
  def isValue: Boolean

  /**
    * is this form of relation a reference?
    *
    * @return
    */
  def isReference: Boolean

  /**
    * is this form of relation a scaslar?
    *
    * @return
    */
  def isScalar: Boolean

  /**
    * is this form of relation a vector?
    *
    * @return
    */
  def isVector: Boolean

  /**
    * is this form of relation a map?
    *
    * @return
    */
  def isMap: Boolean

  /**
   * is this a relation defined externally to the static brio world
   * @return
   */
  def isExtended:Boolean

}

object BrioValueScalarRelation extends BrioRelationFormContext(0, "ValueScalar", isValue = true, isScalar = true, isMap = false)

object BrioValueVectorRelation extends BrioRelationFormContext(1, "ValueVector", isValue = true, isScalar = false, isMap = false)

object BrioValueSetRelation extends BrioRelationFormContext(2, "ValueSet", isValue = true, isScalar = false, isMap = false)

object BrioValueArrayRelation extends BrioRelationFormContext(3, "ValueArray", isValue = true, isScalar = false, isMap = false)

object BrioValueMapRelation extends BrioRelationFormContext(4, "ValueMap", isValue = true, isScalar = false, isMap = true)

object BrioReferenceScalarRelation extends BrioRelationFormContext(5, "ReferenceScalar", isValue = false, isScalar = true, isMap = false)

object BrioReferenceVectorRelation extends BrioRelationFormContext(6, "ReferenceVector", isValue = false, isScalar = false, isMap = false)

object BrioExtendedRelation extends BrioRelationFormContext(-1, "Extended", isValue = false, isScalar = false, isMap = false, isExtended = true)

/**
  *
  * @param code
  * @param name
  * @param isValue
  * @param isScalar
  * @param isMap
  */
case
class BrioRelationFormContext(
                               code: Int,
                               name: String,
                               isValue: Boolean,
                               isScalar: Boolean,
                               isMap: Boolean,
                               isExtended:Boolean  = false
                             ) extends BrioRelationForm {
  override def toString: String = name

  override def isReference: Boolean = !isValue

  override def isVector: Boolean = !isScalar
}

