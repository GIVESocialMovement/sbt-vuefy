import sbt.Keys.scmInfo

enablePlugins(SbtWebBase)

name := "sbt-vuefy"

scalaVersion := "2.12.14"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json"   % "2.8.1",
  "org.mockito"       % "mockito-core" % "3.0.0" % Test,
  "com.lihaoyi"       %% "utest"       % "0.7.1" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

addSbtJsEngine("1.2.3")

addCommandAlias(
  "fmt",
  "all scalafmtSbt scalafmt test:scalafmt"
)
addCommandAlias(
  "check",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

organization := "io.github.tanin47"
organizationName := "tanin47"

Test / publishArtifact := false

organizationHomepage := Some(url("https://github.com/tanin47/sbt-vuefy"))

publishMavenStyle := true
publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
pomIncludeRepository := { _ =>
  false
}
licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT")))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/tanin47/sbt-vuefy"),
    "scm:git@github.com:tanin47/sbt-vuefy.git"
  )
)

developers := List(
  Developer(
    id = "tanin",
    name = "Tanin Na Nakorn",
    email = "@tanin",
    url = url("https://github.com/tanin47")
  )
)

versionScheme := Some("semver-spec")
