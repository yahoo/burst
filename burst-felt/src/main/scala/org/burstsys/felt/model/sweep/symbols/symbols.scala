/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.brio.types.BrioTypes.{BrioTypeName, BrioVersionKey}
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.model.collectors.FeltCollectorSymbols
import org.burstsys.felt.model.control.generate.FeltCtlSymbols
import org.burstsys.felt.model.lattice.FeltLatSymbols
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeSymbols
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.vitals.uid._

package object symbols extends AnyRef with FeltLatSymbols with FeltCtlSymbols with FeltBrioSymbols
  with FeltCollectorSymbols with FeltRuntimeSymbols {

  type FeltVar = String
  type FeltSchemaVar = FeltVar
  type FeltSweepVar = FeltVar

  def normalizedPathName(path: BrioPathName): BrioTypeName = path.replace('.', '_')

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT SCHEMA
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  def schematic(structureTypeName: BrioTypeName, version: BrioVersionKey) = s"schema_structure_${structureTypeName}_V${version}_schematic"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT GLOBALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val schemaSweepSym = "sweep"
  final val feltSchemaSym = "feltTraveler"
  final val brioSchemaSym = "brioSchema"

  final val skipVisitPathSym = "skipVisitPath"
  final val skipTunnelPathSym = "skipTunnelPath"

  final val schemaRuntimeSym = "runtime"
  final val sweepRuntimeSym = "rt"

  final val invocationSym = "invocation"
  final val callExtantParameterSym = s"$invocationSym.call.extantParameter"
  final val callNullParameterSym = s"$invocationSym.call.nullParameter"
  final val callScalarParameterSym = s"$invocationSym.call.scalarParameter"

  final val blobReaderSym = s"reader"
  final val blobDictionarySym = s"$schemaRuntimeSym.dictionary"
  final val blobLatticeSym = s"$schemaRuntimeSym.lattice"
  final val brioPathKeyClass = s"Int"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT Class Names
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val feltRuntimeClass = classOf[FeltRuntime].getName
  final val feltSweepClass = classOf[FeltSweep].getName
  final val feltInvocationClass = classOf[FabricInvocation].getName
  final val feltTravelerClass = classOf[FeltTraveler[_]].getName
  final val teslaReaderClass = classOf[TeslaBufferReader].getName

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  // MISC Class Names
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val vitalsBitMapClass = classOf[VitalsBitMapAnyVal].getName
  final val vitalsUidClass = classOf[VitalsUid].getName
  final val textCodecClass = classOf[VitalsTextCodec].getName

}
