/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.types

import org.burstsys.brio.model.parser.BrioSchemaParser.{BrioSchemaDataTypeClause, BrioSchemaElasticDataType, BrioSchemaLookupDataType, BrioSchemaReferenceScalarRelation, BrioSchemaReferenceVectorRelation, BrioSchemaRelationClause, BrioSchemaSimpleDataType, BrioSchemaValueMapRelation, BrioSchemaValueScalarRelation, BrioSchemaValueVectorRelation}
import org.burstsys.brio.model.parser._
import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.encoding._
import org.burstsys.brio.types.BrioPath.{BrioMissingPathKey, BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.vitals.errors.VitalsException

import scala.language.implicitConversions

/**
  * A single object modeling '''relation''' in a [[BrioStructure]] complex object type
  */
trait BrioRelation extends Any {

  /**
    * TODO
    *
    * @return
    * @deprecated there is actually more than one path per relation so this is broken
    */
  def parentStructure: BrioStructure

  /**
    * The structure types that this relation is valid for
    *
    * @return
    */
  def validTypeSet: Set[BrioTypeKey]

  /**
    * The schema versions that this is valid for
    *
    * @return
    */
  def validVersionSet: Set[BrioVersionKey]

  /**
    * the modeling relation ''form''
    *
    * @return
    */
  def relationForm: BrioRelationForm

  /**
    * The name for this relation
    *
    * @return
    */
  def relationName: BrioRelationName

  /**
    * return either the value datatype key or the reference structure key
    * as appropriate
    */
  def valueOrReferenceTypeKey: BrioTypeKey

  /**
    * the ordinal of the relation within its parent structure
    *
    * @return
    */
  def relationOrdinal: BrioRelationOrdinal

  /**
    * the structure/type for this relation if its a ''reference'' type
    *
    * @return structure or null if not available...
    */
  def referenceStructure: BrioStructure

  /**
    * content datatype for all relations
    *
    * @return
    */
  def valueEncoding: BrioValueEncoding

  /**
    * key datatype for maps
    *
    * @return
    */
  def keyEncoding: BrioValueEncoding

  /**
    * in this relation a ''key'' in parent structure
    *
    * @return
    */
  def isKey: Boolean

  /**
    * in this relation an ''ordinal'' in parent structure
    *
    * @return
    */
  def isOrdinal: Boolean

}

object BrioRelation {

  final
  def apply(schema: BrioSchemaContext, rootStructureTypeKey: BrioTypeKey): BrioRelation =
    BrioRelationContext(
      null,
      schema.rootRelationName, // relation name
      -1, // relation ordinal
      schema.typeKeyToNameMap(rootStructureTypeKey),
      BrioNoValueEncoding, //value encoding
      BrioNoValueEncoding, // key encoding
      BrioReferenceScalarRelation,
      isKey = false,
      isOrdinal = false
    )

  final
  def apply(schema: BrioSchemaContext, structure: BrioStructure, relation: BrioSchemaRelationClause): BrioRelation = {

    val isKey = relation.relationClassifiers.exists(_.isKey)
    val isOrdinal = relation.relationClassifiers.exists(_.isOrdinal)

    relation match {

      case r: BrioSchemaValueScalarRelation =>
        BrioRelationContext(
          structure,
          relation.relationName, relation.relationOrdinal,
          null,
          valueEncoding(schema, r.valueDatatype), //value encoding
          BrioNoValueEncoding, // key encoding
          BrioValueScalarRelation,
          isKey,
          isOrdinal
        )

      case r: BrioSchemaValueVectorRelation =>
        BrioRelationContext(
          structure,
          relation.relationName, relation.relationOrdinal,
          null,
          valueEncoding(schema, r.valueDatatype), //value encoding
          BrioNoValueEncoding, // key encoding
          BrioValueVectorRelation,
          isKey,
          isOrdinal
        )

      case r: BrioSchemaValueMapRelation =>
        BrioRelationContext(
          structure,
          relation.relationName, relation.relationOrdinal,
          null,
          valueEncoding(schema, r.valueDatatype), //value encoding
          valueEncoding(schema, r.keyDatatype), //value encoding
          BrioValueMapRelation,
          isKey,
          isOrdinal
        )

      case r: BrioSchemaReferenceScalarRelation =>
        BrioRelationContext(
          structure,
          relation.relationName, relation.relationOrdinal,
          r.referenceType.selfName,
          BrioNoValueEncoding, //value encoding
          BrioNoValueEncoding, // key encoding
          BrioReferenceScalarRelation,
          isKey,
          isOrdinal
        )

      case r: BrioSchemaReferenceVectorRelation =>
        BrioRelationContext(
          structure,
          relation.relationName, relation.relationOrdinal,
          r.referenceType.selfName,
          BrioNoValueEncoding, //value encoding
          BrioNoValueEncoding, // key encoding
          BrioReferenceVectorRelation,
          isKey,
          isOrdinal
        )

      case _ => throw VitalsException(s"unknown relation=$relation")

    }
  }

  private
  def valueEncoding(schema: BrioSchemaContext, dtc: BrioSchemaDataTypeClause): BrioValueEncoding = {
    dtc match {
      case sdt: BrioSchemaSimpleDataType =>
        sdt.primitive match {
          case "boolean" => BrioBooleanValueEncoding
          case "byte" => BrioByteValueEncoding
          case "short" => BrioShortValueEncoding
          case "integer" => BrioIntegerValueEncoding
          case "long" => BrioLongValueEncoding
          case "double" => BrioDoubleValueEncoding
          case "string" => BrioStringValueEncoding
          case _ => throw BrioSchemaException(dtc.location, s"unknown primitive ${sdt.primitive}")
        }

      // TODO EXTENDED TYPES
      case edt: BrioSchemaElasticDataType =>
        val (offsetName, offsetId) = edt.offsetName match {
          case null => (null, -1)
          case name => (name, schema.elasticOffsetMap.getOrElseUpdate(name, schema.elasticOffsetIndex.getAndIncrement()))
        }
        BrioElasticValueEncoding(edt.bytes, edt.blur, offsetName, offsetId)

      // TODO EXTENDED TYPES
      case ldt: BrioSchemaLookupDataType =>
        val (lookupName, lookupId) = ldt.lookupName match {
          case null => (null, -1)
          case name => (name, schema.lookupTypeTableMap.getOrElseUpdate(name, schema.lookupTypeTableIndex.getAndIncrement()))
        }
        BrioLookupValueEncoding(ldt.bytes, lookupName, lookupId)

      case _ => throw VitalsException(s"unknown dtc=$dtc")
    }
  }
}

final case
class BrioRelationContext(
                           parentStructure: BrioStructure,
                           relationName: BrioRelationName,
                           relationOrdinal: BrioRelationOrdinal,
                           referenceTypeName: BrioTypeName,
                           valueEncoding: BrioValueEncoding,
                           keyEncoding: BrioValueEncoding,
                           relationForm: BrioRelationForm,
                           isKey: Boolean,
                           isOrdinal: Boolean
                         ) extends BrioRelation {

  def duplicate: BrioRelationContext = {
    val copy = this.copy() // TODO clean this up
    copy.referenceStructure = referenceStructure
    copy.validVersionSet = validVersionSet
    copy.validTypeSet = validTypeSet
    copy.relationPathName = relationPathName
    copy.relationPathKey = relationPathKey
    copy
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  lazy val valueOrReferenceTypeKey: BrioTypeKey = if (relationForm.isReference) referenceStructure.structureTypeKey else valueEncoding.typeKey

  var referenceStructure: BrioStructure = _

  var validVersionSet: Set[BrioVersionKey] = _

  var validTypeSet: Set[BrioTypeKey] = _

  var relationPathName: BrioPathName = _

  var relationPathKey: BrioPathKey = _

  override
  def toString: BrioRelationName = s"$relationForm(form=$relationForm, name='$relationPathName')"

}

