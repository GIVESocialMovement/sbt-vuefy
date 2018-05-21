package givers.vuefy

import java.io.File
import java.nio.file.{Files, Paths}

import helpers.BaseSpec
import play.api.libs.json.{JsArray, Json}
import sbt.internal.util.ManagedLogger
import utest._

object CompilerIntegrationSpec extends BaseSpec {

  val tests = Tests {
    'compile - {
      "run webpack and get result correctly" - {
        val targetDir = Files.createTempDirectory("sbt-vuefy-compiler-integration-spec").toFile
        val compiler = new Compiler(
          webpackBinary = new File("./node_modules/.bin/webpack"),
          webpackConfig = new File("./src/test/scala/givers/vuefy/assets/webpack.config.js"),
          sourceDir = new File("./src/test/scala/givers/vuefy/assets"),
          targetDir = targetDir,
          isProd = true,
          logger = mock[ManagedLogger],
          nodeModules =  new File("./node_modules")
        )

        val inputs = Seq(
          Paths.get("./src/test/scala/givers/vuefy/assets/vue/component-a.vue"),
          Paths.get("./src/test/scala/givers/vuefy/assets/vue/dependencies/component-b.vue"),
          Paths.get("./src/test/scala/givers/vuefy/assets/vue/dependencies/component-c.vue")
        )
        val result = compiler.compile(inputs)

        result.success ==> true
        Files.isSameFile(result.entries.head.inputFile.toPath, inputs.head) ==> true
        Files.isSameFile(result.entries(1).inputFile.toPath, inputs(1)) ==> true
        Files.isSameFile(result.entries(2).inputFile.toPath, inputs(2)) ==> true

        result.entries.head.filesWritten.size ==> 1
        result.entries(1).filesWritten.size ==> 1
        result.entries(2).filesWritten.size ==> 1

        Files.exists(result.entries.head.filesWritten.head) ==> true
        Files.exists(result.entries(1).filesWritten.head) ==> true
        Files.exists(result.entries(2).filesWritten.head) ==> true

        Files.isSameFile(result.entries.head.filesWritten.head, Paths.get(s"$targetDir/vue/component-a.js")) ==> true
        Files.isSameFile(result.entries(1).filesWritten.head, Paths.get(s"$targetDir/vue/dependencies/component-b.js")) ==> true
        Files.isSameFile(result.entries(2).filesWritten.head, Paths.get(s"$targetDir/vue/dependencies/component-c.js")) ==> true

        result.entries.head.filesRead.map(_.toFile.getCanonicalPath) ==> inputs.map(_.toFile.getCanonicalPath).toSet
        result.entries(1).filesRead.map(_.toFile.getCanonicalPath) ==> Set(inputs(1).toFile.getCanonicalPath, inputs(2).toFile.getCanonicalPath)
        result.entries(2).filesRead.map(_.toFile.getCanonicalPath) ==> Set(inputs(2).toFile.getCanonicalPath)
      }
    }
  }
}
