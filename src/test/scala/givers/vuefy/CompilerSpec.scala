package givers.vuefy

import java.io.File
import java.nio.file.{Files, Paths}

import helpers.BaseSpec
import play.api.libs.json.{JsArray, Json}
import sbt.internal.util.ManagedLogger
import sbt.{Tests => _, _}
import utest._

import scala.io.Source

object CompilerSpec extends BaseSpec {

  val tests = Tests {
    'compile - {
      val logger = mock[ManagedLogger]
      val shell = mock[Shell]
      val computeDependencyTree = mock[ComputeDependencyTree]
      val prepareWebpackConfig = mock[PrepareWebpackConfig]
      val sourceDir = new File("sourceDir") / "somepath"
      val targetDir = new File("targetDir") / "anotherpath"
      val nodeModulesDir = new File("node_modules")
      val originalConfigFile = sourceDir / "config" / "webpack.config.js"
      val webpackBinaryFile = sourceDir / "binary" / "webpack.binary"
      val compiler = new Compiler(
        webpackBinaryFile,
        originalConfigFile,
        sourceDir,
        targetDir,
        true,
        logger,
        nodeModulesDir,
        shell,
        computeDependencyTree,
        prepareWebpackConfig
      )

      val preparedConfigFile = new File("new") / "webpack" / "prepared-config.js"
      when(prepareWebpackConfig.apply(any(), any())).thenReturn(preparedConfigFile.getAbsolutePath)

      "handles empty" - {
        compiler.compile(Seq.empty) ==> CompilationResult(true, Seq.empty)
        verifyZeroInteractions(shell, logger, computeDependencyTree, prepareWebpackConfig)
      }

      "fails" - {
        when(shell.execute(any(), any(), any())).thenReturn(1)

        val (module1, file1) = Seq("a", "b", "c").mkString(Path.sep.toString) -> (sourceDir / "a" / "b" / "c.vue")
        val (module2, file2) = Seq("a", "b").mkString(Path.sep.toString) -> (sourceDir / "a" / "b.vue")
        val inputPaths = Seq(file1.toPath, file2.toPath)
        val result = compiler.compile(inputPaths)
        result.success ==> false
        result.entries.isEmpty ==> true

        verify(prepareWebpackConfig).apply(
          originalWebpackConfig = originalConfigFile,
          inputs = Seq(Input(module1, file1.toPath), Input(module2, file2.toPath)))
        verifyZeroInteractions(computeDependencyTree)
        verify(shell).execute(
          eq(
            Seq(
              webpackBinaryFile.getCanonicalPath,
              "--config", preparedConfigFile.getAbsolutePath,
              "--output-path", targetDir.getCanonicalPath,
              "-p"
            ).mkString(" ")),
          eq(sourceDir),
          varArgsThat[(String, String)] { varargs =>
            varargs.size == 1 && varargs.head == ("NODE_PATH" -> nodeModulesDir.getCanonicalPath)
          }
        )
      }

      "compiles successfully" - {
        val (module1, file1) = Seq("a", "b", "c").mkString(Path.sep.toString) -> (sourceDir / "a" / "b" / "c.vue")
        val (module2, file2) = Seq("a", "b").mkString(Path.sep.toString) -> (sourceDir / "a" / "b.vue")

        when(shell.execute(any(), any(), any())).thenReturn(0)
        when(computeDependencyTree.apply(any[File]())).thenReturn(
          Map(
            Seq("a", "b", "c.vue").mkString(Path.sep.toString) -> Set(Seq("a", "b", "c.vue").mkString(Path.sep.toString)),
            Seq("a", "b.vue").mkString(Path.sep.toString) -> Set(
              Seq("a", "b.vue").mkString(Path.sep.toString),
              Seq("a", "b", "c.vue").mkString(Path.sep.toString)
            )
          )
        )
        val inputPaths = Seq(file1.toPath, file2.toPath)
        val result = compiler.compile(inputPaths)
        result.success ==> true
        result.entries.size ==> 2

        Files.isSameFile(result.entries.head.inputFile.toPath, inputPaths.head) ==> true
        result.entries.head.filesRead ==> Set((sourceDir / "a" / "b" / "c.vue").toPath)
        result.entries.head.filesWritten ==> Set((targetDir / "a" / "b" / "c.js").toPath)

        Files.isSameFile(result.entries(1).inputFile.toPath, inputPaths(1)) ==> true
        result.entries(1).filesRead ==> Set((sourceDir / "a" / "b.vue").toPath, (sourceDir / "a" / "b" / "c.vue").toPath)
        result.entries(1).filesWritten ==> Set((targetDir / "a" / "b.js").toPath)

        verify(prepareWebpackConfig).apply(
          originalWebpackConfig = originalConfigFile,
          inputs = Seq(Input(module1, file1.toPath), Input(module2, file2.toPath)))
        verify(computeDependencyTree).apply(targetDir / "sbt-vuefy-tree.json")
        verify(shell).execute(
          eq(Seq(
            webpackBinaryFile.getCanonicalPath,
            "--config", preparedConfigFile.getAbsolutePath,
            "--output-path", targetDir.getCanonicalPath,
            "-p"
          ).mkString(" ")),
          eq(sourceDir),
          varArgsThat[(String, String)] { varargs =>
            varargs.size == 1 && varargs.head == ("NODE_PATH" -> nodeModulesDir.getCanonicalPath)
          }
        )
      }
    }

    'getWebpackConfig - {
      val originalWebpackConfig = Files.createTempFile("test", "test")
      val sourceDir = new File("sourceDir") / "somepath"
      val (module1, file1) = Seq("a", "b", "c").mkString(Path.sep.toString) -> (sourceDir / "a" / "b" / "c.vue")

      val webpackConfig = (new PrepareWebpackConfig).apply(
        originalWebpackConfig = originalWebpackConfig.toFile,
        inputs = Seq(Input(module1, file1.toPath)))
      val sbtVuefyFile = new File(webpackConfig).getParentFile / "sbt-vuefy-plugin.js"

      Files.exists(Paths.get(webpackConfig)) ==> true
      Files.exists(sbtVuefyFile.toPath) ==> true

      val src = Source.fromFile(sbtVuefyFile)
      src.mkString ==> Source.fromInputStream(getClass.getResourceAsStream("/sbt-vuefy-plugin.js")).mkString
      src.close()

      Files.deleteIfExists(originalWebpackConfig)
      Files.deleteIfExists(sbtVuefyFile.toPath)
    }

    'buildDependencies - {
      val compute = new ComputeDependencyTree
      def make(s: String) = s"vue${Path.sep}$s"
      val a = make("a")
      val b = make("b")
      val c = make("c")
      val d = make("d")
      val nonVue = "non-vue"

      "builds correctly with flatten" - {
        // Even on window, the path separator from webpack's command is still `/`.
        val jsonStr = JsArray(Seq(
          Json.obj(
            "name" -> "./vue/a",
            "reasons" -> Seq.empty[String]
          ),
          Json.obj(
            "name" -> "./vue/b",
            "reasons" -> Seq("./vue/a + 4 modules")
          ),
          Json.obj(
            "name" -> "./vue/c",
            "reasons" -> Seq("./vue/b + 4 modules")
          ),
          Json.obj(
            "name" -> "./vue/d",
            "reasons" -> Seq("./vue/a + 4 modules")
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
            "name" -> "./vue/a",
            "reasons" -> Seq.empty[String]
          ),
          Json.obj(
            "name" -> nonVue,
            "reasons" -> Seq("./vue/a + 4 modules")
          ),
          Json.obj(
            "name" -> "./vue/c + 4 modules",
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
            "name" -> "./vue/a",
            "reasons" -> Seq("./vue/c + 4 modules")
          ),
          Json.obj(
            "name" -> "./vue/b",
            "reasons" -> Seq("./vue/a + 4 modules")
          ),
          Json.obj(
            "name" -> "./vue/c",
            "reasons" -> Seq("./vue/b + 4 modules")
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
