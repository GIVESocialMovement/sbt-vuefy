package givers.vuefy

import java.io.File
import java.nio.file.{Files, Paths}

import helpers.BaseSpec
import play.api.libs.json.{JsArray, Json}
import sbt.internal.util.ManagedLogger
import utest._

import scala.collection.mutable
import scala.io.Source

object CompilerSpec extends BaseSpec {

  val tests = Tests {
    'compile - {
      val logger = mock[ManagedLogger]
      val shell = mock[Shell]
      val computeDependencyTree = mock[ComputeDependencyTree]
      val prepareWebpackConfig = mock[PrepareWebpackConfig]
      val sourceDir = "/sourceDir/somepath"
      val targetDir = "/targetDir/anotherpath"
      val compiler = new Compiler(
        new File(s"$sourceDir/binary/webpack.binary"),
        new File(s"$sourceDir/config/webpack.config.js"),
        new File(sourceDir),
        new File(targetDir),
        true,
        logger,
        new File("./node_modules"),
        shell,
        computeDependencyTree,
        prepareWebpackConfig
      )

      "handles empty" - {
        compiler.compile(Seq.empty) ==> CompilationResult(true, Seq.empty)
        verifyZeroInteractions(shell, logger, computeDependencyTree, prepareWebpackConfig)
      }

      "fails" - {
        when(shell.execute(any(), any(), any())).thenReturn(1)
        when(prepareWebpackConfig.apply(any())).thenReturn("/new/webpack/config")
        val inputPaths = Seq(
          Paths.get(s"$sourceDir/a/b/c.vue"),
          Paths.get(s"$sourceDir/a/b.vue")
        )
        val result = compiler.compile(inputPaths)
        result.success ==> false
        result.entries.isEmpty ==> true

        verify(prepareWebpackConfig).apply(argThat[File] { arg => Files.isSameFile(Paths.get(s"$sourceDir/config/webpack.config.js"), arg.toPath) })
        verifyZeroInteractions(computeDependencyTree)
        verify(shell).execute(
          eq(Seq(
            s"$sourceDir/binary/webpack.binary",
            "--config", "/new/webpack/config",
            "--output-path", targetDir,
            "-p",
            s"a/b/c=$sourceDir/a/b/c.vue",
            s"a/b=$sourceDir/a/b.vue"
          ).mkString(" ")),
          argThat[File] { arg => Files.isSameFile(arg.toPath, Paths.get(sourceDir)) },
          varArgsThat[(String, String)] { varargs =>
            varargs.size == 1 && varargs.head == ("NODE_PATH" -> new File("./node_modules").getCanonicalPath)
          }
        )
      }

      "compiles successfully" - {
        when(shell.execute(any(), any(), any())).thenReturn(0)
        when(prepareWebpackConfig.apply(any())).thenReturn("/new/webpack/config")
        when(computeDependencyTree.apply(any[File]())).thenReturn(
          Map(
            "./a/b/c.vue" -> Set("./a/b/c.vue"),
            "./a/b.vue" -> Set("./a/b.vue", "./a/b/c.vue")
          )
        )
        val inputPaths = Seq(
          Paths.get(s"$sourceDir/a/b/c.vue"),
          Paths.get(s"$sourceDir/a/b.vue")
        )
        val result = compiler.compile(inputPaths)
        result.success ==> true
        result.entries.size ==> 2

        Files.isSameFile(result.entries.head.inputFile.toPath, inputPaths.head) ==> true
        result.entries.head.filesRead.map(_.toString) ==> Set(s"$sourceDir/./a/b/c.vue")
        result.entries.head.filesWritten.map(_.toString) ==> Set(s"$targetDir/a/b/c.js")

        Files.isSameFile(result.entries(1).inputFile.toPath, inputPaths(1)) ==> true
        result.entries(1).filesRead.map(_.toString) ==> Set(s"$sourceDir/./a/b.vue", s"$sourceDir/./a/b/c.vue")
        result.entries(1).filesWritten.map(_.toString) ==> Set(s"$targetDir/a/b.js")

        verify(prepareWebpackConfig).apply(new File(s"$sourceDir/config/webpack.config.js"))
        verify(computeDependencyTree).apply(argThat[File] { arg =>
          Files.isSameFile(arg.toPath, Paths.get(s"$targetDir/sbt-vuefy-tree.json"))
        })
        verify(shell).execute(
          eq(Seq(
            s"$sourceDir/binary/webpack.binary",
            "--config", "/new/webpack/config",
            "--output-path", targetDir,
            "-p",
            s"a/b/c=$sourceDir/a/b/c.vue",
            s"a/b=$sourceDir/a/b.vue"
          ).mkString(" ")),
          argThat[File] { arg => Files.isSameFile(arg.toPath, Paths.get(sourceDir)) },
          varArgsThat[(String, String)] { varargs =>
            varargs.size == 1 && varargs.head == ("NODE_PATH" -> new File("./node_modules").getCanonicalPath)
          }
        )
      }
    }

    'getWebpackConfig - {
      val originalWebpackConfig = Files.createTempFile("test", "test")
      val webpackConfig = (new PrepareWebpackConfig).apply(originalWebpackConfig.toFile)
      val sbtVuefyFilePath = Paths.get(s"${new File(webpackConfig).getParentFile.getAbsolutePath}/sbt-vuefy-plugin.js")

      Files.exists(Paths.get(webpackConfig)) ==> true
      Files.exists(sbtVuefyFilePath) ==> true
      Source.fromFile(sbtVuefyFilePath.toFile).mkString ==> Source.fromInputStream(getClass.getResourceAsStream("/sbt-vuefy-plugin.js")).mkString

      Files.deleteIfExists(originalWebpackConfig)
      Files.deleteIfExists(sbtVuefyFilePath)
    }

    'buildDependencies - {
      val compute = new ComputeDependencyTree
      def make(s: String) = s"./vue/$s"
      val a = make("a")
      val b = make("b")
      val c = make("c")
      val d = make("d")
      val nonVue = "non-vue"

      "builds correctly with flatten" - {
        val jsonStr = JsArray(Seq(
          Json.obj(
            "name" -> a,
            "reasons" -> Seq.empty[String]
          ),
          Json.obj(
            "name" -> b,
            "reasons" -> Seq(a)
          ),
          Json.obj(
            "name" -> c,
            "reasons" -> Seq(b)
          ),
          Json.obj(
            "name" -> d,
            "reasons" -> Seq(a)
          )
        )).toString

        compute(jsonStr) ==> Map(
          a -> Set(a, b, c, d),
          b -> Set(b, c),
          c -> Set(c),
          d -> Set(d)
        )
      }

      "handles non ./vue correctly" - {
        val jsonStr = JsArray(Seq(
          Json.obj(
            "name" -> a,
            "reasons" -> Seq.empty[String]
          ),
          Json.obj(
            "name" -> nonVue,
            "reasons" -> Seq(a)
          ),
          Json.obj(
            "name" -> c,
            "reasons" -> Seq(nonVue)
          )
        )).toString

        compute(jsonStr) ==> Map(
          a -> Set(a, c),
          c -> Set(c)
        )
      }

      "handles cyclic dependencies" - {
        val jsonStr = JsArray(Seq(
          Json.obj(
            "name" -> a,
            "reasons" -> Seq(c)
          ),
          Json.obj(
            "name" -> b,
            "reasons" -> Seq(a)
          ),
          Json.obj(
            "name" -> c,
            "reasons" -> Seq(b)
          ),
        )).toString()

        compute(jsonStr) ==> Map(
          a -> Set(a, b, c),
          b -> Set(a, b, c),
          c -> Set(a, b, c)
        )
      }
    }
  }
}
