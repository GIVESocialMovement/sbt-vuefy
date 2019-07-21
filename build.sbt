organization := "givers.vuefy"
name := "sbt-vuefy"

lazy val `sbt-vuefy` = (project in file("."))
  .enablePlugins(SbtWebBase)
  .settings(
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.7.4",
      "org.mockito" % "mockito-core" % "3.0.0" % Test,
      "com.lihaoyi" %% "utest" % "0.7.1" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .settings(
    publishMavenStyle := true,
    bintrayOrganization := Some("givers"),
    bintrayRepository := "maven",
    publishArtifact in Test := false,
    pomIncludeRepository := { _ =>
      false
    },
    licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT")))
  )

addSbtJsEngine("1.2.3")
