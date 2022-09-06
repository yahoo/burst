/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.provider

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.provider
import org.burstsys.brio.test.BrioAbstractSpec

class BrioProviderSpec extends BrioAbstractSpec {

  before {
    provider.loadBrioSchemaProviders()
  }

  private case class SchemaTestCase(alias: String, schemaName: String, versions: Int)

  private val cases = Array(
    SchemaTestCase("test", "BrioTest", 2),
    SchemaTestCase("presser", "Presser", 2),
    SchemaTestCase("ultimate", "UltimateSchema", 2),
    SchemaTestCase("quo", "Quo", 3),
    SchemaTestCase("unity", "Unity", 1)
  )

  for (testCase <- cases) {
    it should s"load the ${testCase.schemaName} provider" in {
      val schema = BrioSchema(testCase.alias)
      schema.name should equal(testCase.schemaName)
      schema.versionCount should equal(testCase.versions)
    }
  }

}
