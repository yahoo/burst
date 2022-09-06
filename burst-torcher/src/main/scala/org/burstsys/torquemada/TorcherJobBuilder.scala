/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.logging.log4j.Level
import org.burstsys.catalog
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.motif.Motif
import org.burstsys.torquemada.Parameters.TorcherParameters
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsAgent

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


trait TorcherJobBuilder extends VitalsMetricsAgent {
  this: TorcherJob =>

  val mapper = new ObjectMapper()
  val reportFrequency = 50
  var json: JsonNode = _
  var datasetCountLimit: Long = 0

  def parseSource(torcherParameters: TorcherParameters): Array[Dataset] = {
    assert(torcherParameters.source != null)

    json = mapper.readTree(torcherParameters.source)

    stopOnFail = {
      val sn = json.get("stopOnFail")
      if (sn != null) {
        if (!sn.isBoolean) {
          throw new RuntimeException(s"Field 'stopOnFail' is not a boolean value")
        }
        sn.booleanValue()
      } else false
    }

    datasetCountLimit = {
      val ln = json.get("limit")
      if (ln != null) {
        if (!ln.isValueNode || !ln.isInt) {
          throw new RuntimeException(s"Field 'limit' is not an integer value")
        }
        ln.asInt()
      } else 0
    }

    schemaName = {
      val sn = json.get("schemaName")
      if (sn != null) {
        if (!sn.isTextual) {
          throw new RuntimeException(s"Field 'schemaName' is not a text value")
        }
        sn.textValue()
      } else "unity"
    }
    if (defaultQuery(schemaName) == null)
      throw new RuntimeException(s"Schema '$schemaName' is registered but we don't have a default load query")

    // extract high level params
    concurrency = if (torcherParameters.parallelism > 0) torcherParameters.parallelism else {
      val cc = json.get("parallelism")
      if (cc != null) {
        if (!cc.isValueNode || !cc.isInt) {
          throw new RuntimeException(s"Field 'parallelism' is not an integer value")
        }
        cc.asInt()
      } else 1
    }

    duration = {
      if (torcherParameters.duration != null)
        torcherParameters.duration
      else {
        val ts = json.get("duration")
        if (ts != null) {
          val durText = json.get("duration").asText
          if (durText.trim.toLowerCase == "once")
            Duration(0, TimeUnit.MINUTES) // go though the list once
          else
            Try(Duration(durText)) match {
              case Failure(e) =>
                throw new RuntimeException(s"Unable to parse duration '$ts'", e)
              case Success(d) => d
            }
        } else Duration(0, TimeUnit.MINUTES) // go though the list once
      }
    }

    queryDelay = {
      val queryRate: Double = if (torcherParameters.queryRate > 0) torcherParameters.queryRate else {
        val qr = json.get("queryRate")
        if (qr != null) {
          if (!qr.isValueNode || !qr.isDouble) {
            throw new RuntimeException(s"Field 'queryRate' is not a double value")
          }
          qr.asDouble()
        } else 0.0
      }
      if (queryRate <= 0.0) 0 seconds else
        Duration((concurrency * 1000.0) / queryRate, MILLISECONDS)
    }

    loadDelay = {
      val loadRate: Double = if (torcherParameters.loadRate > 0) torcherParameters.loadRate else {
        val lr = json.get("loadRate")
        if (lr != null) {
          if (!lr.isValueNode || !lr.isDouble) {
            throw new RuntimeException(s"Field 'loadRate' is not a double value")
          }
          lr.asDouble()
        } else 0.0
      }
      if (loadRate <= 0.0) 0 seconds else
        Duration((concurrency * 1000.0) / loadRate, MILLISECONDS)
    }

    // timeout
    timeout = {
      val ts = json.get("timeout")
      if (ts != null) {
        Try(Duration(json.get("timeout").asText)) match {
          case Failure(e) =>
            throw new RuntimeException(s"Unable to parse timeout '$ts'", e)
          case Success(d) => d
        }
      } else Duration(10, TimeUnit.MINUTES)
    }

    //type
    val storeType: Option[String] = {
      val fn = json.get("storeType")
      if (fn != null) {
        if (!fn.isTextual) {
          throw new RuntimeException(s"Field 'storeType' is not a string value")
        }
        Some(fn.asText())
      } else None
    }

    // initialize the default view, query, flush stack
    extractDefaults(json)

    val batches = json.get("batches")
    if (batches == null) {
      throw new RuntimeException(s"No 'batches' array found in source")
    } else if (!batches.isArray) {
      throw new RuntimeException(s"Field 'batches' is not an array")
    }

    val datasets = ArrayBuffer[Dataset]()

    for (b: JsonNode <- batches.asScala) if (this.datasetCountLimit <= 0 || datasets.length < this.datasetCountLimit) {
      // get the defaults
      extractDefaults(b)

      // look for any tags
      val dtn = b.get("domainTag")
      if (dtn != null) {
        if (!dtn.isTextual) {
          throw new RuntimeException(s"Field 'domainTag' is not a string")
        }
        val domainTag = dtn.asText()
        // lookup the tagged domains
        catalogClient.searchDomainsByLabel(domainTag) match {
          case Success(taggedDomains) =>
            for (td <- taggedDomains)  if (this.datasetCountLimit <= 0 || datasets.length < this.datasetCountLimit) {
              createExistingDomainDataset(datasets, td, storeType)
            }
          case Failure(t) =>
            throw new RuntimeException(s"Unable to search domains by label '$domainTag'", t)
        }
      }

      // look for domain array
      val domains = b.get("domains")
      if (domains != null) {
        if (!domains.isArray) {
          throw new RuntimeException(s"Field 'domains' on batch is not an array")
        }

        for (d: JsonNode <- domains.asScala)
          if (this.datasetCountLimit <= 0 || datasets.length < this.datasetCountLimit) {

          // get the defaults
          extractDefaults(d)

          // it's either identified by pk, moniker or projectId but not all three
          val pkn = d.get("pk")
          val mn = d.get("moniker")
          val pidn = d.get("projectId")
          if (Seq(pkn, mn, pidn).count(_ != null) != 1) {
            throw new RuntimeException(s"at least one field of 'pk', 'moniker', or 'projectId' must exist")
          } else if (pkn != null) {
            // primary key lookup
            if (!pkn.isNumber) {
              throw new RuntimeException(s"Field 'pk' is not a number")
            }
            val pk = pkn.asLong()
            catalogClient.findDomainByPk(pk) match {
              case Success(domain) =>
                createExistingDomainDataset(datasets, domain, storeType)
              case Failure(e) =>
                throw e
            }
          } else if (mn != null) {
            // moniker lookup
            if (!mn.isTextual) {
              throw new RuntimeException(s"Field 'moniker' is not a string")
            }
            val moniker = mn.asText()
            catalogClient.findDomainByMoniker(moniker) match {
              case Success(domain) =>
                createExistingDomainDataset(datasets, domain, storeType)
              case Failure(e) =>
                throw e
            }
          } else if (pidn != null) {
            // projectId lookup
            if (!pidn.isNumber) {
              throw new RuntimeException(s"Field 'projectId' is not a number")
            }
            val projectId = pidn.asLong()
            if (storeType.isDefined) {
              catalogClient.searchDomainsByLabel("project_id", Some(projectId.toString)) match {
                case Success(doms) =>
                  if (doms.isEmpty) {
                    notifyListeners(Level.WARN, s"no domain found for project ${projectId.toString}, skipping")
                  } else {
                    if (doms.length > 1)
                      notifyListeners(Level.WARN, burstStdMsg(s"${doms.length} domains with label 'project_id=$projectId' found in catalog"))
                    val domain = doms.head
                    createExistingDomainDataset(datasets, domain, storeType)
                  }
                case Failure(e) =>
                  throw e
              }
            } else {
              createTemporaryDomainDataset(datasets, projectId)
            }
          }

          // finish with the defaults
          finishDefaults(d)
        }
      }

      if (domains == null && dtn == null) {
        throw new RuntimeException(s"No 'domains' array or 'domainTag' found in batch")
      }

      // finish with the defaults
      finishDefaults(b)
    }

    notifyListeners(Level.INFO, s"completed analysis of ${datasets.size} items")
    datasets.toArray
  }

  def createExistingDomainDataset(datasets: ArrayBuffer[Dataset], domain: CatalogDomain,
                                  storeType: Option[String]): Unit = {
    // stop if we hit a limit
    if (this.datasetCountLimit > 0 && datasets.length >= this.datasetCountLimit)
      return
    val otherId: String = domain.domainProperties.getOrElse(CatalogDomain.DomainProjectIdProperty, null)
    // check for fuse or not
    val viewPk: Long = if (storeType.isDefined) {
      // search for fuse views
      catalogClient allViewsForDomain domain.pk match {
        case Success(views) =>
          views.find(
            v => v.storeProperties.getOrElse("burst.store.name", "") == storeType.get &&
                 v.schemaName == schemaName
          ) match {
            case Some(v) =>
              v.pk
            case None =>
              notifyListeners(Level.WARN, s"no store type '${storeType.get}' with schema '$schemaName' found for domain ${domain.pk}, skipping")
              0
          }
        case Failure(e) =>
          notifyListeners(Level.ERROR, s"unable to fetch views for domain ${domain.pk}: $e")
          0
      }
    } else {
      validateMotif(0, motifDefaultStack.head)
      createTemporaryView(domain.pk, motifDefaultStack.head,
        tempViewStorePropertiesDefaultStack.head, tempViewLabelsDefaultStack.head)
    }
    // generic Add dataset call
    if (viewPk != 0) {
      datasets += Dataset(otherId, domain.pk, viewPk, motifDefaultStack.head,
        queriesDefaultStack.head, flushDefaultStack.head, info = buildInfo(domain), storeType.isEmpty)
      if (datasets.size > 1 && datasets.size % reportFrequency == 0)
      // tell us about progress
        notifyListeners(Level.INFO, s"analyzed ${datasets.size} items")
    }
  }

  def createTemporaryDomainDataset(datasets: ArrayBuffer[Dataset], projectId: Long): Unit = {
    if (this.datasetCountLimit > 0 && datasets.length >= this.datasetCountLimit)
      return
    validateMotif(0, motifDefaultStack.head)
    val tDomain = createTorcherDomain(projectId)
    val viewPk: Long = createTemporaryView(tDomain.pk, motifDefaultStack.head,
      tempViewStorePropertiesDefaultStack.head, tempViewLabelsDefaultStack.head)
    // generic Add dataset call
    if (viewPk != 0) {
      datasets += Dataset(otherId = projectId.toString, domainId = tDomain.pk, viewId = viewPk,
        motif = motifDefaultStack.head, queries = queriesDefaultStack.head, flush = flushDefaultStack.head,
        info = buildInfo(tDomain) , domainTemporary = true)
      if (datasets.size > 1 && datasets.size % reportFrequency == 0)
      // tell us about progress
        notifyListeners(Level.INFO, s"analyzed ${datasets.size} items")
    }
  }

  /**
    * Look for a set of defaults attached to this node.  If one or more exist then push them onto the defaults stack
    */
  def extractDefaults(node: JsonNode): Unit = {
    val fn = node.get("flush")
    if (fn != null) {
      if (!fn.isValueNode || !fn.isTextual) {
        throw new RuntimeException(s"Field 'flush' is not a string value")
      }
      val mode = fn.asText().toUpperCase.trim
      if (mode.isEmpty)
        throw new RuntimeException(s"Field 'flush' is not a valid value")
      val f = mode(0)
      if (f != 'N' && f != 'F' && f != 'G') {
        throw new RuntimeException(s"Field 'flush' is not one of the valid values 'N', 'F', or 'G'")
      }
      flushDefaultStack = f +: flushDefaultStack
    }

    // motif default
    val mn = node.get("motif")
    if (mn != null) {
      if (!mn.isValueNode || !mn.isTextual) {
        throw new RuntimeException(s"Field 'motif' is not a string value")
      }
      motifDefaultStack = mn.asText().trim +: motifDefaultStack
    }

    // query defaults
    val qn = node.get("queries")
    if (qn != null) {
      if (!qn.isArray) {
        throw new RuntimeException(s"Field 'queries' is not an array")
      }

      val queries: Seq[String] = (for (b: JsonNode <- qn.asScala) yield {
        // build query list
        if (!b.isValueNode || !b.isTextual) {
          throw new RuntimeException(s"Element of 'queries' array must be a string value")
        }
        b.asText().trim
      }).toSeq

      queriesDefaultStack = queries.filter(_.nonEmpty).toList +: queriesDefaultStack
    }

    // temporary view store properties defaults
    val tvpn = node.get("temporaryViewStoreProperties")
    if (tvpn != null) {
      if (!tvpn.isObject)
        throw new RuntimeException(s"Field 'temporaryViewStoreProperties' is not an object")

      val props: Map[String, String] = (for (f <- tvpn.fields.asScala) yield {
        if (!f.getValue.isValueNode) {
          throw new RuntimeException(s"Elements of 'temporaryViewStorePropertes' object must be value fields")
        }
        f.getKey -> f.getValue.asText()
      }).toMap
      tempViewStorePropertiesDefaultStack = (
        if (tempViewStorePropertiesDefaultStack.nonEmpty)
          tempViewStorePropertiesDefaultStack.head ++ props
        else
          props
      ) +: tempViewStorePropertiesDefaultStack
    }

    // temporary view labels defaults
    val tvln = node.get("temporaryViewLabels")
    if (tvln != null) {
      if (!tvln.isObject)
        throw new RuntimeException(s"Field 'temporaryViewLabels' is not an object")

      val props: Map[String, String] = (for (f <- tvln.fields.asScala) yield {
        if (!f.getValue.isValueNode || !f.getValue.isTextual) {
          throw new RuntimeException(s"Elements of 'temporaryViewLabels' object must be string fields")
        }
        f.getKey -> f.getValue.asText()
      }).toMap
      tempViewLabelsDefaultStack = (
        if (tempViewLabelsDefaultStack.nonEmpty)
          tempViewLabelsDefaultStack.head ++ props
        else
          props
      ) +: tempViewLabelsDefaultStack
    }
  }

  /**
    * Look for a set of defaults attached to this node.  If one or more exist then remove the top most entry from the
    * defaults stack
    */
  def finishDefaults(node: JsonNode): Unit = {
    val fn = node.get("flush")
    if (fn != null) {
      flushDefaultStack = flushDefaultStack.tail
      assert(flushDefaultStack.nonEmpty)
    }

    // motif default
    val mn = node.get("motif")
    if (mn != null) {
      motifDefaultStack = motifDefaultStack.tail
      assert(motifDefaultStack.nonEmpty)
    }

    // query defaults
    val qn = node.get("queries")
    if (qn != null) {
      queriesDefaultStack = queriesDefaultStack.tail
      assert(queriesDefaultStack.nonEmpty)
    }

    val tvspn = node.get("tempViewStoreProperties")
    if (tvspn != null) {
      tempViewStorePropertiesDefaultStack = tempViewStorePropertiesDefaultStack.tail
      assert(tempViewStorePropertiesDefaultStack.nonEmpty)
    }

    val tvsln = node.get("tempViewLabels")
    if (tvsln != null) {
      tempViewLabelsDefaultStack = tempViewLabelsDefaultStack.tail
      assert(tempViewLabelsDefaultStack.nonEmpty)
    }
  }

  // this is the stack of defaults currently in play for the dataset
  final var motifDefaultStack: List[String] = List(defaultViewMotif)
  final var queriesDefaultStack: List[List[String]] = List(List())
  final var flushDefaultStack: List[Char] = List('N')
  final var tempViewStorePropertiesDefaultStack: List[Map[String, String]] = List(defaultViewStoreProperties)
  final var tempViewLabelsDefaultStack: List[Map[String, String]] = List(defaultViewLabels)

  val motif: Motif = Motif.build()

  def buildInfo(domain: CatalogDomain): String = {
    val projectLabel = if (domain.labels.nonEmpty && domain.labels.get.contains(CatalogDomain.DomainProjectIdProperty))
      s"label=${domain.labels.get(CatalogDomain.DomainProjectIdProperty)} "
    else ""
    s"$projectLabel'${domain.moniker}'"
  }

  def createTorcherDomain(projectId: Long): CatalogDomain = {
    val domainLabels = Some(Map(catalog.torcherDataLabel -> "true",
      CatalogDomain.DomainProjectIdProperty -> s"$projectId"))
    val domainProperties = Map(CatalogDomain.DomainProjectIdProperty -> s"$projectId",
      CatalogDomain.DomainBeastProjectIdProperty -> s"$projectId")
    val dmn: CatalogDomain = CatalogDomain(0, s"Torcher-$projectId-${System.nanoTime()}",
      domainProperties = domainProperties, labels = domainLabels)
    catalogClient.ensureDomain(dmn) match {
      case Success(dPk) =>
        notifyListeners(Level.DEBUG, s"created temporary domain $dPk")
        dmn.copy(pk = dPk)
      case Failure(e) =>
        throw new RuntimeException(s"failed to create domain for project $projectId: $e")
    }
  }

  def cleanTemporaryViews(): Unit = {
    val totalDatasets = datasets.length

    this.synchronized {
      for ((d, i) <- datasets.zipWithIndex if d.viewTemporary || d.domainTemporary) {
        if (i != 0 && (i % reportFrequency == 0 || i + 1 == datasets.length))
          notifyListeners(Level.INFO, s"deleted ${i + 1}/${datasets.length} temporary views")
        catalogClient deleteView d.viewId match {
          case Success(vPk) =>
            notifyListeners(Level.DEBUG, s"deleted temporary view $vPk")
          case Failure(e) =>
            notifyListeners(Level.ERROR, s"failed to delete temporary view ${d.viewId}: $e")
        }
        if (d.domainTemporary)
          catalogClient deleteDomain d.domainId match {
            case Success(dPk) =>
              notifyListeners(Level.DEBUG, s"deleted temporary domain $dPk")
            case Failure(e) =>
              notifyListeners(Level.ERROR, s"failed to delete temporary domain ${d.domainId}: $e")
          }
      }
      datasets = Array.empty
    }

    notifyListeners(Level.INFO, s"deleted $totalDatasets temporary views")
  }

  def createTemporaryView(domainPk: Long, motif: String,
                          storeProperties: Map[String, String], viewLabels: Map[String, String]): Long = {
    val view = s"Torcher-${System.nanoTime()}"
    val v = CatalogView(0, view, domainPk, schemaName, storeProperties = storeProperties, viewMotif = motif, labels = Some(viewLabels), udk = Some(view))
    catalogClient.ensureView(v) match {
      case Success(vPk) =>
        notifyListeners(Level.DEBUG, s"created temporary view $vPk")
        vPk
      case Failure(e) =>
        throw new RuntimeException(s"failed to create view for domain $domainPk" , e)
    }
  }

  def validateMotif(num: Int, source: String): Boolean = {
    // special key of star replaces the default view, but test it for validity
    Try(motif.parseView(schemaName, source)) match {
      case Success(_) => true
      case Failure(e) =>
        throw new RuntimeException(s"Default view declaration at $num is invalid", e)
    }
  }
}
