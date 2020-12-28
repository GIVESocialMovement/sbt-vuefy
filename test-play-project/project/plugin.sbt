lazy val root =
  Project("plugins", file(".")).aggregate(sbtVuefy).dependsOn(sbtVuefy)
lazy val sbtVuefy = RootProject(file("./..").getCanonicalFile.toURI)
resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.typesafe.play" % "sbt-plugin"   % "2.8.7")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.0.0")
