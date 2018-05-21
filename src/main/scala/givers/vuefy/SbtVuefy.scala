package givers.vuefy


import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.web._
import com.typesafe.sbt.web.incremental._
import sbt.Keys._
import sbt._
import xsbti.{Position, Problem, Severity}

import scala.io.Source

object SbtVuefy extends AutoPlugin {
  override def requires: Plugins = SbtWeb
  override def trigger: PluginTrigger = AllRequirements

  object autoImport {
    object VueKeys {
      val vuefy = TaskKey[Seq[File]]("vuefy", "Generate compiled Javascripts files from Vue components.")
      val webpackBinary = TaskKey[String]("vuefyWebpackBinary", "The binary location for webpack.")
      val webpackConfig = TaskKey[String]("vuefyWebpackConfig", "The location for webpack config.")
      val prodCommands = TaskKey[Set[String]]("vuefyProdCommands", "A set of SBT commands that triggers production build. The default is `stage`. In other words, use -p (as opposed to -d) with webpack.")
    }
  }

  import autoImport.VueKeys._

  val baseSbtVuefySettings = Seq(
    excludeFilter in vuefy := HiddenFileFilter || "_*",
    includeFilter in vuefy := "*.vue",
    prodCommands := Set("stage"),
    webpackBinary := "please-define-the-binary",
    webpackConfig := "please-define-the-config-location.js",
    resourceManaged in vuefy := webTarget.value / "vuefy" / "main",
    managedResourceDirectories in Assets+= (resourceManaged in vuefy in Assets).value,
    resourceGenerators in Assets += vuefy in Assets,
    vuefy in Assets := task.dependsOn(WebKeys.webModules in Assets).value
  )


  override def projectSettings: Seq[Setting[_]] = inConfig(Assets)(baseSbtVuefySettings)

  lazy val task = Def.task {
    val sourceDir = (sourceDirectory in Assets).value
    val targetDir = (resourceManaged in vuefy in Assets).value
    val logger = (streams in Assets).value.log
    val vuefyReporter = (reporter in Assets).value
    val prodCommandValues = (prodCommands in vuefy).value
    val isProd = state.value.currentCommand.exists { exec => prodCommandValues.contains(exec.commandLine) }
    val webpackBinaryLocation = (webpackBinary in vuefy).value
    val webpackConfigLocation = (webpackConfig in vuefy).value

    val sources = (sourceDir ** ((includeFilter in vuefy in Assets).value -- (excludeFilter in vuefy in Assets).value)).get

    implicit val fileHasherIncludingOptions = OpInputHasher[File] { f =>
      OpInputHash.hashString(Seq(
        f.getCanonicalPath,
        isProd,
        sourceDir.getAbsolutePath
      ).mkString("--"))
    }

    val results = incremental.syncIncremental((streams in Assets).value.cacheDirectory / "run", sources) { modifiedSources =>
      val startInstant = System.currentTimeMillis

      if (modifiedSources.nonEmpty) {
        logger.info(s"[Vuefy] Compile on ${modifiedSources.size} changed files")
      } else {
        logger.info(s"[Vuefy] No changes to compile")
      }

      val compiler = new Compiler(webpackBinaryLocation, webpackConfigLocation, sourceDir, targetDir, isProd, logger)

      // Compile all modified sources at once
      val result = compiler.compile(modifiedSources.map(_.toPath))

      // Report compilation problems
      CompileProblems.report(
        reporter = vuefyReporter,
        problems = if (!result.success) {
          Seq(new Problem {
            override def category() = ""

            override def severity() = Severity.Error

            override def message() = ""

            override def position() = new Position {
              override def line() = java.util.Optional.empty()

              override def lineContent() = ""

              override def offset() = java.util.Optional.empty()

              override def pointer() = java.util.Optional.empty()

              override def pointerSpace() = java.util.Optional.empty()

              override def sourcePath() = java.util.Optional.empty()

              override def sourceFile() = java.util.Optional.empty()
            }
          })
        } else { Seq.empty }
      )

      // Collect OpResults
      val opResults: Map[File, OpResult] = result.entries
        .map { entry =>
          entry.inputFile -> OpSuccess(entry.filesRead.map(_.toFile), entry.filesWritten.map(_.toFile))
        }
        .toMap

      // Collect the created files
      val createdFiles = result.entries.flatMap(_.filesWritten.map(_.toFile))

      val endInstant = System.currentTimeMillis

      if (createdFiles.nonEmpty) {
        logger.info(s"[Vuefy] finished compilation in ${endInstant - startInstant} ms and generated ${createdFiles.size} JS files")
      }

      (opResults, createdFiles)

    }(fileHasherIncludingOptions)

    // Return the dependencies
    (results._1 ++ results._2.toSet).toSeq
  }
}
