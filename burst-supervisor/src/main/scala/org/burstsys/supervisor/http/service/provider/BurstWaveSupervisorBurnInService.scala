/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.provider

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.fabric.wave.data.model.generation.FabricGenerationIdentity
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricViewKey}
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.vitals.errors
import org.burstsys.vitals.properties.VitalsPropertyMap

import java.util.logging.Level
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration

trait BurstWaveBurnInListener {
  def burnInStarted(config: BurnInConfig): Unit = {}

  def burnInEvent(event: BurnInEvent): Unit = {}

  def burnInStopped(): Unit = {}
}

trait BurstWaveSupervisorBurnInService {
  def startBurnIn(config: BurnInConfig): Unit

  def stopBurnIn(): Unit

  def getEvents: Array[BurnInEvent]

  def getConfig: BurnInConfig

  def talksTo(listeners: BurstWaveBurnInListener*): Unit
}

final case
class BurnInConfig(
                    /** a maximum limit for how long this burn-in test should run */
                    maxDuration: Option[Duration],

                    /** a set of batches used to test they system */
                    batches: Array[BurnInBatch],
                  ) {

  def validate(): (Boolean, Array[String]) = {
    val errors = ArrayBuffer[String]()

    maxDuration match {
      case Some(duration) =>
        if (duration.toMillis < 0) {
          errors += "maxDuration must be positive or empty"
        } else {
          if (batches != null && batches.exists(b => b.maxDuration.exists(d => d > duration))) {
            errors += "batch maxDuration must not be greater than gloabl maxDuration"
          }
        }
      case None =>
    }

    if (batches == null || batches.length < 1) {
      errors += "at least one batch must be specified"

    } else for (batch <- batches) {
      val (_, batchErrors) = batch.validate()
      errors ++= batchErrors
    }

    (errors.isEmpty, errors.toArray)
  }

}

object BurnInBatch {
  object DurationType {
    val Duration = "duration"
    val Datasets = "datasets"
  }
}

final case
class BurnInBatch(
                   /** number of concurrent datasets */
                   concurrency: Int,

                   /** a list of datasets to be queries */
                   datasets: Array[BurnInDatasetDescriptor],

                   /** the query to use when loading a dataset, if not specified by the dataset descriptor */
                   defaultLoadQuery: Option[String],

                   /** a list of queries to be executed against each dataset on each iteration */
                   queries: Array[String],

                   /** how long should this batch run: a specified length of time, or a number of datasets */
                   durationType: String, // one of ["duration", "datasets"]

                   /** the number of datasets that should be run */
                   desiredDatasetIterations: Option[Int],

                   /** the length of time datasets in this batch should continue to be loaded */
                   desiredDuration: Option[Duration],

                   /** an upper limit for how long this batch can run, only used if `durationType` is set to `datasets` */
                   maxDuration: Option[Duration],
                 ) {
  def validate(): (Boolean, Array[String]) = {
    val errors = ArrayBuffer[String]()

    if (concurrency < 1) {
      errors += "batch.concurrency must be positive"
    }

    if (datasets == null || datasets.isEmpty) {
      errors += "batch must contain at least one dataset"
    }

    if (queries == null || queries.isEmpty) {
      errors += "batch must specify at least one query"
    }

    durationType match {
      case BurnInBatch.DurationType.Datasets =>
        desiredDatasetIterations match {
          case Some(iterations) =>
            if (iterations < 1) {
              errors += "batch.desiredDatasetIterations must be positive"
            }
          case None =>
            errors += "batch.desiredDatasetIterations must be specified when durationType == 'datasets'"
        }
      case BurnInBatch.DurationType.Duration =>
        desiredDuration match {
          case Some(duration) =>
            if (duration.toMillis <= 0) {
              errors += "batch.desiredDuration must be positive"
            }
            if (maxDuration.exists(max => max < duration)) {
              errors += "batch.desiredDuration must be less than maxDuration when maxDuration is present"
            }
          case None =>
            errors += "batch.desiredDuration must be specified when durationType == 'duration'"
        }

      case _ =>
        errors += "batch.durationType must be either 'duration' or 'datasets'"
    }

    if (maxDuration.exists(d => d.toMillis < 1)) {
      errors += "batch.maxDuration must be positive when specified"
    }

    if (defaultLoadQuery.isEmpty && datasets != null && datasets.exists(ds => ds.loadQuery.isEmpty)) {
      errors += "batch.dataset.loadQuery must be specified when batch.defaultLoadQuery is not specified"
    }

    if (datasets != null) for (dataset <- datasets) {
      val (_, datasetErrors) = dataset.validate()
      errors ++= datasetErrors
    }

    (errors.isEmpty, errors.toArray)
  }
}

object BurnInDatasetDescriptor {
  object Source {
    val ByPk = "byPk"
    val ByUdk = "byUdk"
    val ByProperty = "byProperty"
    val Generate = "generate"
  }
}

final case
class BurnInDatasetDescriptor(
                               /** how is this dataset defined, loading by pk, udk, matching labels, or generated from an inline definition */
                               datasetSource: String, // one of ["byPk", "byUdk", "byProperty", "generate"]

                               /** the pk of the view to copy, only used when datasetSource == byPk */
                               pk: Option[Long],

                               /** the udk of the view to copy, only used when datasetSource == byUdk */
                               udk: Option[String],

                               /** a property that must be present on the view, only used when datasetSource == byProperty */
                               propertyKey: Option[String],
                               propertyValue: Option[String],

                               /** a domain definition used to create this dataset, only used when datasetSource == generate */
                               domain: Option[BurnInDomain],

                               /** a view definition used to create this dataset, only used when datasetSource == generate */
                               view: Option[BurnInView],

                               /** the query that used for the initial dataset load */
                               loadQuery: Option[String],

                               /** force a reload of this dataset (by increasing the generationClock) after every N queries */
                               reloadEvery: Option[Int],
                             ) {

  def validate(): (Boolean, Array[String]) = {
    val errors = ArrayBuffer[String]()

    datasetSource match {
      case BurnInDatasetDescriptor.Source.ByPk =>
        if (pk.isEmpty) {
          errors += "batch.dataset.pk must be specified when datasetSource == 'byPk'"
        }

      case BurnInDatasetDescriptor.Source.ByUdk =>
        if (udk.isEmpty) {
          errors += "batch.dataset.udk must be specified when datasetSource == 'byUdk'"
        }

      case BurnInDatasetDescriptor.Source.ByProperty =>
        if (propertyKey.isEmpty) {
          errors += "batch.dataset.propertyKey must be specified when datasetSource == 'byProperty'"
        }

      case BurnInDatasetDescriptor.Source.Generate =>
        if (domain.isEmpty) {
          errors += "batch.dataset.domain must be specified when datasetSource == 'generate'"
        }
        if (view.isEmpty) {
          errors += "batch.dataset.view must be specified when datasetSource == 'generate'"
        }
      case _ =>
        errors += "batch.dataset.datasetSource must one of 'byPk', 'byUdk', 'byProperty', 'generate'"
    }

    (errors.isEmpty, errors.toArray)
  }
}

final case class BurnInDomain(
                               domainProperties: VitalsPropertyMap
                             ) extends FabricDomain {
  override def domainKey: FabricDomainKey = ???
}

final case class BurnInView(
                             schemaName: BrioSchemaName,
                             storeProperties: VitalsPropertyMap,
                             viewMotif: String,
                             viewProperties: VitalsPropertyMap,
                           ) extends FabricView {
  override def domainKey: FabricDomainKey = ???

  override def viewKey: FabricViewKey = ???

  override def generationClock: FabricGenerationClock = ???

  override def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricView = ???

  override def init(gm: FabricGenerationIdentity): FabricView = ???
}

abstract class BurnInEvent(val time: Long = System.currentTimeMillis())

final case class BurnInLogEvent(message: String, level: Level = Level.INFO) extends BurnInEvent()

final case class BurnInStatsEvent(
                                   datasetsIterated: Long,
                                   totalBytesLoaded: Long,
                                   totalScanTime: Long,
                                   totalClockTime: Long,
                                   // copy needful items from torcher's DatasetStatistics
                                 ) extends BurnInEvent()
