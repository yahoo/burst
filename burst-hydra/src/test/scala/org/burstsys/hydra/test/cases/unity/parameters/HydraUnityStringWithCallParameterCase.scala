/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parameters

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityStringWithCallParameterCase extends HydraUseCase(
  73, 73, "unity",
  parameters = s"""{ one:"hello" }"""
) {

  /*
    override val sweep = new B895A6E99D29A4A86BE946EB017CF5D94
    override val serializeTraversal = true
  */

  override def analysisSource: String =
    s"""
       |hydra $analysisName(
       |	one:string = "test"
       |) {
       |	schema unity
       |	frame $frameName {
       |		cube user {
       |			limit = 100
       |			dimensions {
       |				one:verbatim[string]
       |			}
       |		}
       |		user => {
       |			pre => 			{
       |				$analysisName.$frameName.one = $analysisName.one
       |			}
       |			post => 			{
       |				insert($analysisName.$frameName)
       |			}
       |		}
       |	}
       |}     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    r.rowSet.map {
      row =>
        val cell = row.cells(0)
        if (cell.isNull) null else cell.asString
    } should equal(
      // should be null since this is not in the input dictionary
      Array(null)
    )
  }


}
