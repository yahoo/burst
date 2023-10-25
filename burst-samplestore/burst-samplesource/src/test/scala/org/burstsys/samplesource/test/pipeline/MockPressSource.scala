/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.test.pipeline

import org.burstsys.brio.press._

final case class MockPressSource() extends BrioPressSourceBase with BrioPressSource {

  override
  def extractRootReferenceScalar(): PresserInstance = {
    MockPressModel
  }

  override
  def extractReferenceScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance): BrioPressInstance = {
    parentInstance match {
      case o: RootStructure => cursor.relationName match {
        case "f3" => o.f3
        case "added" => o.added
        case "application" => o.application
      }
      case o: SecondLevelStructure => cursor.relationName match {
        case "f4" => o.f4
      }
      case o: ApplicationStructure => cursor.relationName match {
        case "firstUse" => o.firstUse
        case "mostUse" => o.mostUse
        case "lastUse" => o.lastUse
      }
    }
  }

  override
  def extractReferenceVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance): Iterator[BrioPressInstance] = {
    parentInstance match {
      case o: RootStructure => cursor.relationName match {
        case "f4" => o.f4.sortBy(_.f1).iterator // make sure its sorted
      }
      case o: SecondLevelStructure => cursor.relationName match {
        case "f3" => o.f3.sortBy(_.f1).iterator // make sure its sorted
      }
    }
  }

  override
  def extractValueScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueScalarPressCapture): Unit = {
    parentInstance match {
      case o: RootStructure => cursor.relationName match {
        case "f0" => extractStringValueScalar(capture, o.f0)
        case "f1" => capture.longValue(o.f1)
        case "f2" => capture.shortValue(o.f2)
      }
      case o: SecondLevelStructure => cursor.relationName match {
        case "f0" => capture.longValue(o.f0)
        case "f1" => capture.longValue(o.f1)
        case "f2" => capture.doubleValue(o.f2)
      }
      case o: ThirdLevelStructure => cursor.relationName match {
        case "f0" => capture.longValue(o.f0)
        case "f1" => capture.longValue(o.f1)
      }
      case o: AddedStructure => cursor.relationName match {
        case "f0" => extractStringValueScalar(capture, o.f0)
        case "f1" => capture.doubleValue(o.f1)
        case "f3" => capture.booleanValue(o.f3)
        case "f4" => capture.booleanValue(o.f4)
      }
      case o: UseStructure => cursor.relationName match {
        case "tag" => extractStringValueScalar(capture, o.tag)
      }
    }
  }

  override
  def extractValueVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueVectorPressCapture): Unit = {
    parentInstance match {
      case o: ThirdLevelStructure => cursor.relationName match {
        case "f3" => extractDoubleValueVector(capture, o.f3)
      }
      case o: AddedStructure => cursor.relationName match {
        case "f2" => extractStringValueVector(capture, o.f2)
      }
    }
  }

  override
  def extractValueMap(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueMapPressCapture): Unit = {
    parentInstance match {
      case o: ThirdLevelStructure => cursor.relationName match {
        case "f2" => extractStringStringMap(capture, o.f2)
      }
    }
  }
}
