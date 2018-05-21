package givers.vuefy

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path}

import play.api.libs.json.Reads._
import play.api.libs.json.{JsArray, Json}
import sbt.internal.util.ManagedLogger

import scala.io.Source

case class CompilationResult(success: Boolean, entries: Seq[CompilationEntry])
case class CompilationEntry(inputFile: File, filesRead: Set[Path], filesWritten: Set[Path])
case class Input(name: String, path: Path)


object Compiler {
}

class Shell {
  def execute(cmd: String, cwd: File, envs: (String, String)*): Int = {
    import scala.sys.process._

    Process(cmd, cwd, envs:_*).!
  }
}

class ComputeDependencyTree {
  def apply(file: File): Map[String, Set[String]] = {
    apply(scala.io.Source.fromFile(file).mkString)
  }

  def apply(content: String): Map[String, Set[String]] = {
    val json = Json.parse(content)

    val deps = json.as[JsArray].value
      .flatMap { obj =>
        val name = obj("name").as[String]
        val relations = obj("reasons").as[Seq[String]].map { reason =>
          reason -> name
        }

        relations ++ Seq(name -> name) // the file also depends on itself.
      }
      .groupBy { case (key, _) => key }
      .mapValues(_.map(_._2).toSet)

    flatten(deps)
      // We only care about our directory.
      .filter { case (key, _) => key.startsWith("./vue/")}
      .mapValues(_.filter(_.startsWith("./vue/")))
  }

  private[this] def flatten(deps: Map[String, Set[String]]): Map[String, Set[String]] = {
    var changed = false
    val newDeps = deps
      .map { case (key, children) =>
        val newChildren = children ++ children.flatMap { v => deps.getOrElse(v, Set.empty) }
        if (newChildren.size != children.size) { changed = true }
        key -> newChildren
      }

    if (changed) {
      flatten(newDeps)
    } else {
      newDeps
    }
  }
}

class PrepareWebpackConfig {
  def apply(originalWebpackConfig: String) = {
    import sbt._

    val tmpDir = Files.createTempDirectory("sbt-vuefy")
    val targetFile = tmpDir.toFile / "webpack.config.js"

    Files.copy(new File(originalWebpackConfig).toPath, targetFile.toPath)

    new PrintWriter(tmpDir.toFile / "sbt-vuefy-plugin.js") {
      try {
        write(Source.fromInputStream(getClass.getResourceAsStream("/sbt-vuefy-plugin.js")).mkString)
      } finally {
        close()
      }
    }

    targetFile.getAbsolutePath
  }
}

class Compiler(
  webpackBinary: String,
  webpackConfig: String,
  sourceDir: File,
  targetDir: File,
  isProd: Boolean,
  logger: ManagedLogger,
  shell: Shell = new Shell,
  dependencyComputer: ComputeDependencyTree = new ComputeDependencyTree,
  prepareWebpackConfig: PrepareWebpackConfig = new PrepareWebpackConfig
) {

  def compile(inputFiles: Seq[Path]): CompilationResult = {
    import sbt._

    if (inputFiles.isEmpty) {
      return CompilationResult(success = true, entries = Seq.empty)
    }

    val inputs = inputFiles.map { inputFile =>
      val name = sourceDir.toPath.relativize((inputFile.getParent.toFile / inputFile.toFile.base).toPath).toString
      Input(name, inputFile)
    }

    val cmd = (
      Seq(
        sourceDir.toPath.relativize(new File(webpackBinary).getAbsoluteFile.toPath).toString, // Because we run on sourceDir.
        "--config", prepareWebpackConfig.apply(webpackConfig),
        "--output-path", targetDir.getAbsolutePath,
        if (isProd) { "-p" } else { "-d" }
      ) ++ inputs.map { input =>
        s"""${input.name}=${input.path.toAbsolutePath.toString}"""
      }
    ).mkString(" ")

    logger.info(cmd)
    val exitCode = shell.execute(cmd, sourceDir, "NODE_PATH" -> new File("./node_modules").getAbsolutePath)
    val success = exitCode == 0

    CompilationResult(
      success = success,
      entries = if (success) {
        val dependencyMap = dependencyComputer.apply(targetDir / "sbt-vuefy-tree.json")
        inputs
          .map { input =>
            val outputRelativePath = sourceDir.toPath.relativize((input.path.getParent.toFile / s"${input.path.toFile.base}.js").toPath).toString
            val outputFile = targetDir / outputRelativePath

            val dependencies = dependencyMap
              .getOrElse(s"./${input.name}.vue", Set.empty)
              .map { relativePath =>
                (sourceDir / relativePath).toPath
              }

            CompilationEntry(input.path.toFile, dependencies, Set(outputFile.toPath))
          }
      } else {
        Seq.empty
      }
    )
  }
}
