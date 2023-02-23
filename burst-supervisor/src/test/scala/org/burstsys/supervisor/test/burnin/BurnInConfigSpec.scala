/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.burnin

import org.burstsys.supervisor.http.service.provider.BurnInConfig
import org.burstsys.supervisor.test.support.BurstSupervisorSpec
import org.burstsys.vitals
import org.burstsys.vitals.errors

class BurnInConfigSpec extends BurstSupervisorSpec {

  private val mapper = vitals.json.buildJsonMapper

  private val validDataset =
    """{
      |
      |}""".stripMargin

  private val validBatch =
    s"""{
       |  "concurrency": 1,
       |  "datasets": [$validDataset],
       |  "queries": [],
       |  "durationType": "",
       |  "maxDuration": "1 hour"
       |}""".stripMargin

  "Burn-In - config" should "not specify negative maxDuration" in {
    validateConfig(s"""{"maxDuration":"-1 hours"}""") { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("maxDuration must be positive or empty")
    }

    validateConfig(s"""{}""") { config =>
      val (_, errors) = config.validate()
      errors should not contain("maxDuration must be positive or empty")
    }

    validateConfig(s"""{"maxDuration":"1 hours"}""") { config =>
      val (_, errors) = config.validate()
      errors should not contain("maxDuration must be positive or empty")
    }
  }

  it should "specify at least one batch" in {
    validateConfig("""{}""") { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("at least one batch must be specified")
    }

    validateConfig("""{"batches": []}""") { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("at least one batch must be specified")
    }

    validateConfig("""{"batches": [{}]}""") { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should not contain("at least one batch must be specified")
    }
  }

  "Burn-In - batch" should "not specify a batch-level maxDuration longer than the global maxDuration" in {
    validateConfig(s"""{"maxDuration": "30 minutes", "batches": [{"maxDuration": "1 hour"}]}""") { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch maxDuration must not be greater than gloabl maxDuration")
    }

    validateConfig(s"""{"maxDuration": "30 minutes", "batches": [{"maxDuration": "15 minutes"}]}""") { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch maxDuration must not be greater than gloabl maxDuration")
    }

  }

  it should "specify a positive concurrency" in {
    validateConfig(
      s"""{
         |  "batches":[{
         |    "concurrency": -1
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.concurrency must be positive")
    }

    validateConfig(
      s"""{
         |  "batches":[{
         |    "concurrency": 0
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.concurrency must be positive")
    }

    validateConfig(
      s"""{
         |  "batches":[{
         |    "concurrency": 1
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.concurrency must be positive")
    }
  }

  it should "specify a valid maxDuration" in {
    validateConfig(
      s"""{
         |  "batches":[{
         |    "maxDuration": "-1 hour"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.maxDuration must be positive when specified")
    }

    validateConfig(
      s"""{
         |  "batches":[{
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.maxDuration must be positive when specified")
    }

    validateConfig(
      s"""{
         |  "batches":[{
         |    "maxDuration": "1 hour"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.maxDuration must be positive when specified")
    }
  }

  it should "specify at least one dataset per batch" in {
    validateConfig(
      s"""{
         |  "batches":[{
         |    "datasets": []
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch must contain at least one dataset")
    }

    validateConfig(
      s"""{
         |  "batches":[{
         |    "datasets": [{}]
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch must contain at least one dataset")
    }

  }

  it should "specify at least one query per batch" in {
    validateConfig(
      s"""{
         |  "batches": [{
         |    "queries": []
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch must specify at least one query")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "queries": [""]
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch must specify at least one query")
    }

  }

  it should "speicfy a valid durationType" in {
    validateConfig(
      s"""{
         |  "batches": [{
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.durationType must be either 'duration' or 'datasets'")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "nope"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.durationType must be either 'duration' or 'datasets'")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "duration"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.durationType must be either 'duration' or 'datasets'")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "datasets"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.durationType must be either 'duration' or 'datasets'")
    }

  }

  it should "specify a valid desiredDruation when durationType == duration" in {
    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "duration"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain ("batch.desiredDuration must be specified when durationType == 'duration'")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "duration",
         |    "desiredDuration": "-1 hour"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.desiredDuration must be positive")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "duration",
         |    "desiredDuration": "1 hour",
         |    "maxDuration": "30 minutes"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.desiredDuration must be less than maxDuration when maxDuration is present")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "duration",
         |    "desiredDuration": "1 hour"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.desiredDuration must be specified when durationType == 'duration'")
      errors should not contain("batch.desiredDuration must be positive")
      errors should not contain("batch.desiredDuration must be less than maxDuration when maxDuration is present")
    }

  }

  it should "specify a valid desiredDatasetIterations" in {
    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "datasets"
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.desiredDatasetIterations must be specified when durationType == 'datasets'")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "datasets",
         |    "desiredDatasetIterations": -1
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.desiredDatasetIterations must be positive")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "datasets",
         |    "desiredDatasetIterations": 0
         |  }]
         |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.desiredDatasetIterations must be positive")
    }

    validateConfig(
      s"""{
         |  "batches": [{
         |    "durationType": "datasets",
         |    "desiredDatasetIterations": 1
         |  }]
         |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.desiredDatasetIterations must be specified when durationType == 'datasets'")
      errors should not contain("batch.desiredDatasetIterations must be positive")
    }

  }

  it should "ensure every dataset has a load query" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{"loadQuery": ""}, {}]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.loadQuery must be specified when batch.defaultLoadQuery is not specified")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "defaultLoadQuery": "",
        |     "datasets": [{"loadQuery": ""}, {}]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should not contain("batch.dataset.loadQuery must be specified when batch.defaultLoadQuery is not specified")
    }
  }

  "Burn-In - dataset" should "specify a valid source" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": ""
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byPk"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byUdk"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byProperty"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "generate"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain("batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'")
    }

  }

  it should "specify a dataset by pk" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byPk"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.pk must be specified when datasetSource == 'byPk'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byPk",
        |       "pk": 12345
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should not contain("batch.dataset.pk must be specified when datasetSource == 'byPk'")
    }
  }

  it should "specify a dataset by udk" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byUdk"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.udk must be specified when datasetSource == 'byUdk'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byUdk",
        |       "udk": "some_udk"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should not contain ("batch.dataset.udk must be specified when datasetSource == 'byUdk'")
    }

  }

  it should "specify a dataset by property" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byProperty"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.propertyKey must be specified when datasetSource == 'byProperty'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "byProperty",
        |       "propertyKey": "some_key"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (_, errors) = config.validate()
      errors should not contain ("batch.dataset.propertyKey must be specified when datasetSource == 'byProperty'")
    }

  }

  it should "generate a dataset" in {
    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "generate"
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.domain must be specified when datasetSource == 'generate'")
      errors should contain("batch.dataset.view must be specified when datasetSource == 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "generate",
        |       "view": {}
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.domain must be specified when datasetSource == 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "generate",
        |       "domain": {}
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should contain("batch.dataset.view must be specified when datasetSource == 'generate'")
    }

    validateConfig(
      """{
        |   "batches": [{
        |     "datasets": [{
        |       "datasetSource": "generate",
        |       "domain": {},
        |       "view": {}
        |     }]
        |  }]
        |}""".stripMargin
    ) { config =>
      val (isValid, errors) = config.validate()
      isValid shouldBe false
      errors should not contain("batch.dataset.domain must be specified when datasetSource == 'generate'")
      errors should not contain("batch.dataset.view must be specified when datasetSource == 'generate'")
    }

  }


  private def validateConfig(config: String)(validate: BurnInConfig => {}): Unit = {
    val burnInConfig = mapper.readValue(config, classOf[BurnInConfig])
    validate(burnInConfig)
  }

}
