lazy val `sbt-vuefy` = project in file(".")

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "com.lihaoyi" %% "utest" % "0.6.3" % Test
)

organization := "givers.vuefy"

name := "sbt-vuefy"

publishMavenStyle := true

bintrayOrganization := Some("givers")

bintrayRepository := "maven"

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT")))

testFrameworks += new TestFramework("utest.runner.Framework")

addSbtJsEngine("1.2.2")
