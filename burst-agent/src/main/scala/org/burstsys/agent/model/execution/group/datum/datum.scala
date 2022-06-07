/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.group

import org.burstsys.agent.api.BurstQueryApiDatum._
import org.burstsys.agent.api.BurstQueryDataType.BooleanType
import org.burstsys.agent.api.{BurstQueryApiDataForm, BurstQueryApiDatum, BurstQueryDataType}
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.execute.parameters.{FabricMapForm, FabricParameterForm, FabricScalarForm, FabricVectorForm}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._

import scala.language.implicitConversions

package object datum extends VitalsLogger {

  type AgentDatum = BurstQueryApiDatum

  final
  def datatypeFor(bType: BrioTypeKey): BurstQueryDataType = {
    bType match {
      case BrioBooleanKey => BooleanType
      case BrioByteKey => BurstQueryDataType.ByteType
      case BrioShortKey => BurstQueryDataType.ShortType
      case BrioIntegerKey => BurstQueryDataType.IntegerType
      case BrioLongKey => BurstQueryDataType.LongType
      case BrioDoubleKey => BurstQueryDataType.DoubleType
      case BrioStringKey => BurstQueryDataType.StringType
      case _ => throw VitalsException(s"bad datatype=$bType")
    }
  }

  final
  def datatypeOf(datatype: BurstQueryDataType): BrioTypeKey = {
    datatype match {
      case BooleanType => BrioBooleanKey
      case BurstQueryDataType.ByteType => BrioByteKey
      case BurstQueryDataType.ShortType => BrioShortKey
      case BurstQueryDataType.IntegerType => BrioIntegerKey
      case BurstQueryDataType.LongType => BrioLongKey
      case BurstQueryDataType.DoubleType => BrioDoubleKey
      case BurstQueryDataType.StringType => BrioStringKey
      case _ => throw VitalsException(s"bad datatype=$datatype")
    }
  }

  final
  def datumToAnys(d: AgentDatum): Any = {
    d match {
      case d: BooleanData => d.booleanData
      case d: ByteData => d.byteData
      case d: ShortData => d.shortData
      case d: IntegerData => d.integerData
      case d: LongData => d.longData
      case d: DoubleData => d.doubleData
      case d: StringData => d.stringData
      case _ => throw VitalsException(s"bad datatype=$d")
    }
  }


  implicit def fabricFormToApiForm(form: FabricParameterForm): BurstQueryApiDataForm = {
    form match {
      case FabricScalarForm => BurstQueryApiDataForm.Scalar
      case FabricVectorForm => BurstQueryApiDataForm.Vector
      case FabricMapForm => BurstQueryApiDataForm.Map
      case _ => ???
    }
  }

  implicit def apiFormToFabricForm(form: BurstQueryApiDataForm): FabricParameterForm = {
    form match {
      case BurstQueryApiDataForm.Scalar => FabricScalarForm
      case BurstQueryApiDataForm.Vector => FabricVectorForm
      case BurstQueryApiDataForm.Map => FabricMapForm
      case _ => ???
    }
  }
}
