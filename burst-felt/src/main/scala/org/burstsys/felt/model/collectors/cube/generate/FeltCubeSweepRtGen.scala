/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.generate.{routeInstanceVariable, routeRootVariable}
import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.collectors.shrub.generate.{shrubInstanceVariable, shrubRootVariable}
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.collectors.tablet.generate.{tabletInstanceVariable, tabletRootVariable}
import org.burstsys.felt.model.sweep.symbols.{brioMutableDictionaryClass, collectorClassName}
import org.burstsys.felt.model.tree.code.{FeltNoCode, _}

trait FeltCubeSweepRtGen extends Any {

  self: FeltCubeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode = {

    def cubeVariables(implicit cursor: FeltCodeCursor): FeltCode = analysis.cubes.map {
      cube =>
        val cubeName = cube.cubeName
        s"""|
            |${C(s"root variables for '$cubeName'")}
            |${I}var ${cubeRoot(cubeName)}:${binding.collectors.cubes.collectorClassName} = _ ;
            |${I}var ${cubeDictionary(cubeName)}:$brioMutableDictionaryClass = _ ;
            |${generateCubeVariables(cubeName)}""".stripMargin
    }.mkString

    s"""$generateCubeDictionaryLookup$generateCubeDictionaryAssign$generateRootCubeLookup$generateRootCubeAssign$cubeVariables""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def generateCubeVariables(cubeName: String)(implicit cursor: FeltCodeCursor): FeltCode = {

    def relations(implicit cursor: FeltCodeCursor): FeltCode = {
      (for (r <- allCubeTargetNodes) yield {
        val pathName = r.pathName
        val space = FeltStaticCubeSpace(global, cubeName)
        // only put in variables that end up being used...
        if (!space.currentInstanceUsed(pathName) && !space.currentRelationUsed(pathName)) FeltNoCode
        else {
          s"""|
              |${C(s"cube static variables for '$cubeName' -> '$pathName' ")}
              |${I}var ${cubeInstanceVariable(cubeName, pathName)}:${binding.collectors.cubes.collectorClassName} = _ ;
              |${I}var ${cubeRelationVariable(cubeName, pathName)}:${binding.collectors.cubes.collectorClassName} = _ ;  """.stripMargin
        }
      }).mkString
    }

    s"""|
        |${C(s"path/cube exchange variables for '$cubeName'")}
        |$relations""".stripMargin
  }

  final private
  def generateRootCubeLookup(implicit cursor: FeltCodeCursor): FeltCode = {
    def cases(implicit cursor: FeltCodeCursor): FeltCode = analysis.frames.map {
      frame =>
        frame.collectorDecl match {
          case cube: FeltCubeDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${cubeRoot(cube.cubeName)} ;  // '${frame.frameName}' """.stripMargin
          case route: FeltRouteDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${routeInstanceVariable(route.routeName)} ;  // '${frame.frameName}' """.stripMargin
          case tablet: FeltTabletDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${tabletInstanceVariable(tablet.tabletName)} ;  // '${frame.frameName}' """.stripMargin
          case shrub: FeltShrubDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${shrubInstanceVariable(shrub.shrubName)} ;  // '${frame.frameName}' """.stripMargin
          case _ =>
            s"""|
                |${I}case ${frame.frameId} ⇒ null; // '${frame.frameName}'""".stripMargin
        }

    }.mkString

    s"""|
        |${C("read root collector out of frame for plane")}
        |$I@inline override
        |${I}def frameCollector(frameId: Int):$collectorClassName = {
        |${I2}frameId match {
        |${cases(cursor indentRight 2)}
        |${I3}case _ ⇒ ??? ;
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def generateRootCubeAssign(implicit cursor: FeltCodeCursor): FeltCode = {

    def cases(implicit cursor: FeltCodeCursor): FeltCode = analysis.frames.map {
      frame =>
        frame.collectorDecl match {
          case cube: FeltCubeDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${cubeRoot(cube.cubeName)} = collector.asInstanceOf[${binding.collectors.cubes.collectorClassName}] ;  // ${frame.frameName}""".stripMargin
          case route: FeltRouteDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${routeRootVariable(route.routeName)} = collector.asInstanceOf[${binding.collectors.routes.collectorClassName}] ;  // ${frame.frameName}""".stripMargin
          case tablet: FeltTabletDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${tabletRootVariable(tablet.tabletName)} = collector.asInstanceOf[${binding.collectors.tablets.collectorClassName}] ;  // ${frame.frameName}""".stripMargin
          case shrub: FeltShrubDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${shrubRootVariable(shrub.shrubName)} = collector.asInstanceOf[${binding.collectors.shrubs.collectorClassName}] ;  // ${frame.frameName}""".stripMargin
          case _ =>
            s"""|
                |${I}case ${frame.frameId} ⇒  null ;  // '${frame.frameName}'""".stripMargin
        }
    }.mkString

    s"""|
        |${C("write root collector into frame from plane")}
        |$I@inline override
        |${I}def frameCollector(frameId: Int, collector:$collectorClassName):Unit = {
        |${I2}frameId match {
        |${cases(cursor indentRight 2)}
        |${I3}case _ ⇒ ??? ;
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def generateCubeDictionaryLookup(implicit cursor: FeltCodeCursor): FeltCode = {

    def cases(implicit cursor: FeltCodeCursor): FeltCode = analysis.frames.map {
      frame =>
        frame.collectorDecl match {
          case cube: FeltCubeDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${cubeDictionary(cube.cubeName)};  // '${frame.frameName}'""".stripMargin
          case _ =>
            s"""|
                |${I}case ${frame.frameId} ⇒ null ;  // '${frame.frameName}' """.stripMargin
        }
    }.mkString

    s"""|
        |${C("read dictionary from frame to write to plane")}
        |$I@inline override
        |${I}def frameDictionary(frameId: Int): $brioMutableDictionaryClass = {
        |${I2}frameId match {
        |${cases(cursor indentRight 2)}
        |${I3}case _ ⇒ ??? ;
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def generateCubeDictionaryAssign(implicit cursor: FeltCodeCursor): FeltCode = {

    def cases(implicit cursor: FeltCodeCursor): FeltCode = analysis.frames.map {
      frame =>
        frame.collectorDecl match {
          case cube: FeltCubeDecl =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ${cubeDictionary(cube.cubeName)} = dictionary ;  // '${frame.frameName}'""".stripMargin
          case _ =>
            s"""|
                |${I}case ${frame.frameId} ⇒ ;  // '${frame.frameName}' """.stripMargin
        }

    }.mkString

    s"""|
        |${C("write dictionary to frame from plane")}
        |$I@inline override
        |${I}def frameDictionary(frameId: Int, dictionary: $brioMutableDictionaryClass): Unit = {
        |${I2}frameId match {
        |${cases(cursor indentRight 2)}
        |${I3}case _ ⇒ ??? ;
        |$I2}
        |$I}""".stripMargin
  }


}
