/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.types

import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.vitals.errors.VitalsException

import scala.reflect.ClassTag

/**
 * A Felt construct representing a ''type'' definition. Almost
 * all 'type' questions are answered here.
 */
trait FeltType extends Any with Equals {

  /**
   * the brio schema [[BrioRelationForm]] for this type
   *
   * @return
   */
  def form: BrioRelationForm

  /**
   * The Brio type identifier for the ''value''
   *
   * @return
   */
  def valueType: BrioTypeKey

  /**
   * The Brio type identifier for the ''key'' (if this type has a key e.g. for a map)
   *
   * @return
   */
  def keyType: BrioTypeKey

  final
  def unitTypeType: Boolean = valueType == BrioUnitTypeKey

  final
  def anyTypeType: Boolean = valueType == BrioAnyTypeKey

  final
  def noKeyType: Boolean = keyType == BrioAnyTypeKey

  final
  def isFixedValue: Boolean = valueType == BrioByteKey | valueType == BrioShortKey | valueType == BrioIntegerKey | valueType == BrioLongKey

  final
  def isString: Boolean = valueType == BrioStringKey

  ////////////////////////////////////////////////////////////////////////////////////////
  // GENERATED CODE (type information textual representations generated to scala code and compiled)
  ////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the scala ''value'' type as a text string.
   * This is the source text that is placed into generated and compiled scala code.
   * '''NOTE:''' Lexicon Strings are converted to [[Short]]
   *
   * @return
   */
  def valueTypeAsCode(implicit cursor: FeltCodeCursor): String

  /**
   * the scala ''value'' type default value a text string.
   * This is the source text that is placed into generated and compiled scala code.
   * '''NOTE:''' Lexicon Strings are converted to [[Short]]
   *
   * @return
   */
  def valueDefaultAsCode(implicit cursor: FeltCodeCursor): String

  /**
   * the scala ''key'' type as a text string
   * This is the source text that is placed into generated and compiled scala code.
   * '''NOTE:''' Lexicon Strings are converted to [[Short]]
   *
   * @return
   */
  def keyTypeAsCode(implicit cursor: FeltCodeCursor): String

  /**
   * the scala ''key'' type default value a text string.
   * This is the source text that is placed into generated and compiled scala code.
   * '''NOTE:''' Lexicon Strings are converted to [[Short]]
   *
   * @return
   */
  def keyDefaultAsCode(implicit cursor: FeltCodeCursor): String

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ALTERNATE TEXTUAL REPRESENTATIONS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * this the 'felt' naming for this type i.e. this is used
   * by hydra.
   *
   * @return
   */
  def valueTypeAsFelt: FeltCode


  def keyTypeAsFelt: FeltCode

  /**
   * This is the BRIO schema language name for this type
   *
   * @return
   */
  def valueTypeAsBrio: String

}

object FeltType {

  def any: FeltType = FeltTypeContext()

  def unit: FeltType = FeltTypeContext(valueType = BrioUnitTypeKey)

  def refVec(): FeltType =
    FeltTypeContext(valueType = BrioAnyTypeKey, keyType = BrioAnyTypeKey, BrioReferenceVectorRelation)

  def refScal(): FeltType =
    FeltTypeContext(valueType = BrioAnyTypeKey, keyType = BrioAnyTypeKey, BrioReferenceScalarRelation)

  def valScal[T <: BrioDataType : ClassTag]: FeltType = valScal(brioDataTypeFromClassTag[T])

  def valScal(valueType: BrioTypeKey): FeltType =
    FeltTypeContext(valueType = valueType, keyType = BrioAnyTypeKey, BrioValueScalarRelation)

  def valVec(valueType: BrioTypeKey): FeltType =
    FeltTypeContext(valueType = valueType, keyType = BrioAnyTypeKey, BrioValueVectorRelation)

  def valArray(valueType: BrioTypeKey): FeltType =
    FeltTypeContext(valueType = valueType, keyType = BrioAnyTypeKey, BrioValueArrayRelation)

  def valSet(valueType: BrioTypeKey): FeltType =
    FeltTypeContext(valueType = valueType, keyType = BrioAnyTypeKey, BrioValueSetRelation)

  def valMap(valueType: BrioTypeKey, keyType: BrioTypeKey): FeltType =
    FeltTypeContext(valueType = valueType, keyType = keyType, BrioValueMapRelation)

  def combine(typeList: FeltType*): FeltType = {
    if (typeList.isEmpty)
      return unit
    if (typeList.exists(_.anyTypeType))
      return any
    var current = typeList.head
    typeList.foreach {
      t => if (t == current) current = t
    }
    current
  }

  def boolean: FeltType = valScal[Boolean]

  def byte: FeltType = valScal[Byte]

  def short: FeltType = valScal[Short]

  def integer: FeltType = valScal[Int]

  def long: FeltType = valScal[Long]

  def double: FeltType = valScal[Double]

}

private case
class FeltTypeContext(
                       valueType: BrioTypeKey = BrioAnyTypeKey,
                       keyType: BrioTypeKey = BrioAnyTypeKey,
                       form: BrioRelationForm = BrioValueScalarRelation
                     ) extends FeltType {

  override
  def toString: String = {
    form match {
      case BrioValueScalarRelation => valueTypeAsFelt
      case BrioValueMapRelation => s"map[$keyTypeAsFelt, $valueTypeAsFelt]"
      case BrioValueVectorRelation => s"array[$valueTypeAsFelt]"
      case BrioValueSetRelation => s"set[$valueTypeAsFelt]"
      case BrioReferenceScalarRelation => s"ref:"
      case BrioReferenceVectorRelation => s"ref[]"
      case f =>
        throw VitalsException(s"Unknown form $f")
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // EQUALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def hashCode(): BrioRelationCount = {
    form match {
      case BrioValueScalarRelation =>
        31 * (31 * this.form.hashCode) + this.valueType.hashCode

      case BrioValueVectorRelation =>
        31 * (31 * this.form.hashCode) + this.valueType.hashCode

      case BrioValueMapRelation =>
        31 * (31 * (31 * this.form.hashCode) + this.valueType.hashCode)

      case BrioReferenceScalarRelation => ???

      case BrioReferenceVectorRelation => ???
      case _ => ???
    }

  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: FeltTypeContext =>
        if (that.form != this.form)
          return false
        form match {
          case BrioValueScalarRelation => this.valueType == that.valueType

          case BrioValueVectorRelation => this.valueType == that.valueType

          case BrioValueMapRelation => this.keyType == that.keyType && this.valueType == that.valueType

          case BrioReferenceScalarRelation =>
            false // can't make reference scalars equal to each other yet

          case BrioReferenceVectorRelation =>
            false // can't make reference vectors equal to each other yet

          case BrioValueSetRelation =>
            ???

          case _ =>
            throw VitalsException(s"attempt to compare unknow form $form")
        }
      case _ => false
    }
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[FeltType]

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ALTERNATE TEXTUAL REPRESENTATIONS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def valueTypeAsFelt: FeltCode =
    valueType match {
      case BrioAnyTypeKey => "any"
      case BrioUnitTypeKey => "unit"
      case BrioBooleanKey => "boolean"
      case BrioByteKey => "byte"
      case BrioShortKey => "short"
      case BrioIntegerKey => "integer"
      case BrioLongKey => "long"
      case BrioDoubleKey => "double"
      case BrioStringKey => "string"
      case t => throw VitalsException(s" unknown value type  $valueType")
    }

  final override
  def keyTypeAsFelt: FeltCode =
    keyType match {
      case BrioAnyTypeKey => "any"
      case BrioUnitTypeKey => "unit"
      case BrioBooleanKey => "boolean"
      case BrioByteKey => "byte"
      case BrioShortKey => "short"
      case BrioIntegerKey => "integer"
      case BrioLongKey => "long"
      case BrioDoubleKey => "double"
      case BrioStringKey => "string"
      case t => throw VitalsException(s" unknown value type  $valueType")
    }

  final override def valueTypeAsBrio: String = {
    brioDataTypeNameFromKey(valueType)
  }

  ////////////////////////////////////////////////////////////////////////////////////////
  // GENERATED CODE
  ////////////////////////////////////////////////////////////////////////////////////////

  final override
  def valueTypeAsCode(implicit cursor: FeltCodeCursor): String =
    valueType match {
      case BrioAnyTypeKey => "Any"
      case BrioUnitTypeKey => "Unit"
      case BrioBooleanKey => "Boolean"
      case BrioByteKey => "Byte"
      case BrioShortKey => "Short"
      case BrioIntegerKey => "Int"
      case BrioLongKey => "Long"
      case BrioDoubleKey => "Double"
      case BrioStringKey => if (cursor.global.lexicon.enabled) "Short" else "String"
      case t => throw VitalsException(s" unknown value type  $valueType")
    }

  final override
  def keyTypeAsCode(implicit cursor: FeltCodeCursor): String =
    keyType match {
      case BrioAnyTypeKey => "Any"
      case BrioUnitTypeKey => "Unit"
      case BrioBooleanKey => "Boolean"
      case BrioByteKey => "Byte"
      case BrioShortKey => "Short"
      case BrioIntegerKey => "Int"
      case BrioLongKey => "Long"
      case BrioDoubleKey => "Double"
      case BrioStringKey => if (cursor.global.lexicon.enabled) "Short" else "String"
      case t => throw VitalsException(s" unknown key type  $keyType")
    }


  final override
  def valueDefaultAsCode(implicit cursor: FeltCodeCursor): String = {
    if (cursor.global.lexicon.enabled && valueType == BrioStringKey)
      defaultValueForBrioTypeKeyAsString(BrioShortKey)
    else
      defaultValueForBrioTypeKeyAsString(valueType)
  }

  final override
  def keyDefaultAsCode(implicit cursor: FeltCodeCursor): String = {
    if (cursor.global.lexicon.enabled && valueType == BrioStringKey)
      defaultValueForBrioTypeKeyAsString(BrioShortKey)
    else
      defaultValueForBrioTypeKeyAsString(keyType)
  }

}

