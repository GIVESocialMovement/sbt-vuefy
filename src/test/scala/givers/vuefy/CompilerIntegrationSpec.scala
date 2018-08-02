package givers.vuefy

import java.io.File
import java.nio.file.Files

import helpers.BaseSpec
import sbt.internal.util.ManagedLogger
import sbt.{Tests => _, _}
import utest._

object CompilerIntegrationSpec extends BaseSpec {

  val tests = Tests {
    'compile - {
      "run webpack and get result correctly" - {
        val targetDir = Files.createTempDirectory("sbt-vuefy-compiler-integration-spec").toFile
        val compiler = new Compiler(
          webpackBinary = new File("node_modules") / ".bin" / "webpack",
          webpackConfig = new File("src") / "test" / "scala" / "givers" / "vuefy" / "assets" / "webpack.config.js",
          sourceDir = new File("src") / "test" / "scala" / "givers" / "vuefy" / "assets",
          targetDir = targetDir,
          isProd = true,
          logger = mock[ManagedLogger],
          nodeModules =  new File("node_modules")
        )

        val inputs = Seq(
          new File("src") / "test" / "scala" / "givers" / "vuefy" / "assets" / "vue" / "component-a.vue",
          new File("src") / "test" / "scala" / "givers" / "vuefy" / "assets" / "vue" / "dependencies/component-b.vue",
          new File("src") / "test" / "scala" / "givers" / "vuefy" / "assets" / "vue" / "dependencies/component-c.vue"
        )
        val result = compiler.compile(inputs.map(_.toPath))

        result.success ==> true
        result.entries.head.inputFile ==> inputs.head
        result.entries(1).inputFile ==> inputs(1)
        result.entries(2).inputFile ==> inputs(2)

        result.entries.head.filesWritten.size ==> 1
        result.entries(1).filesWritten.size ==> 1
        result.entries(2).filesWritten.size ==> 1

        Files.exists(result.entries.head.filesWritten.head) ==> true
        Files.exists(result.entries(1).filesWritten.head) ==> true
        Files.exists(result.entries(2).filesWritten.head) ==> true

        result.entries.head.filesWritten.head ==> (targetDir / "vue" / "component-a.js").toPath
        result.entries(1).filesWritten.head ==> (targetDir / "vue" / "dependencies" / "component-b.js").toPath
        result.entries(2).filesWritten.head ==> (targetDir / "vue" / "dependencies" / "component-c.js").toPath

        result.entries.head.filesRead ==> inputs.map(_.toPath).toSet
        result.entries(1).filesRead ==> Set(inputs(1).toPath, inputs(2).toPath)
        result.entries(2).filesRead ==> Set(inputs(2).toPath)
      }
    }
  }
}
