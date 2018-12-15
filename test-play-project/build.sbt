name := """test-play-project"""
organization := "givers.vuefy"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, SbtVuefy)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  guice
)

Assets / VueKeys.vuefy / VueKeys.prodCommands := Set("stage")
Assets / VueKeys.vuefy / VueKeys.webpackBinary := {
  // Detect windows
  if (sys.props.getOrElse("os.name", "").toLowerCase.contains("win")) {
    (new File(".") / "node_modules" / ".bin" / "webpack.cmd").getAbsolutePath
  } else {
    (new File(".") / "node_modules" / ".bin" / "webpack").getAbsolutePath
  }
}
Assets / VueKeys.vuefy / VueKeys.webpackConfig := (new File(".") / "webpack.config.js").getAbsolutePath
