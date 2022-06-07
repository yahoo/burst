/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.io.InputStream
import java.util.Properties

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.property

package object git extends VitalsLogger {

  final val vitalsValidateBuildKey = "burst.common.git.validate.build"

  final val gitPropertiesFile = "burst-git.properties"

  def vitalsValidateBuildProperty: Boolean = property[Boolean](vitalsValidateBuildKey, true)

  /** *
    * for testing with intellij and stale maven builds.
    */
  def turnOffBuildValidation(): Unit = System.setProperty(vitalsValidateBuildKey, false.toString)

  private lazy val properties = {
    val p = new Properties()
    val inputStream: InputStream = getClass.getResourceAsStream(gitPropertiesFile)
    if (inputStream == null)
      log warn burstStdMsg(s"resource $gitPropertiesFile not found")
    else
      p.load(inputStream)
    p
  }

  private def stringProperty(prop: String): String = {
    val p = properties.get(prop)
    if(p == null) "" else p.toString
  }

  def branch: String = stringProperty("git.branch")

  def describe: String = stringProperty("git.commit.id.describe")

  def describeShort: String = stringProperty("git.commit.id.describe-short")

  def commitId: String = stringProperty("git.commit.id")

  def buildVersion: String = stringProperty("git.build.version")

  def buildUserName: String = stringProperty("git.build.user.name")

  def buildUserEmail: String = stringProperty("git.build.user.email")

  def buildTime: String = stringProperty("git.build.time")

  def commitUserName: String = stringProperty("git.commit.user.name")

  def commitUserEmail: String = stringProperty("git.commit.user.email")

  def commitMessageShort: String = stringProperty("git.commit.message.short")

  def commitMessageFull: String = stringProperty("git.commit.message.full")

  def commitTime: String = stringProperty("git.commit.time")

  final
  val burstBuildPropertyName = "burst.build"

  final
  def buildVersionFlag: String = s"-D$burstBuildPropertyName=$commitId"

}
