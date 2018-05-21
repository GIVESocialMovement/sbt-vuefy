name := """test-play-project"""
organization := "givers.vuefy"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, SbtVuefy)

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  guice
)

Assets / VueKeys.vuefy / VueKeys.prodCommands := Set("stage")
Assets / VueKeys.vuefy / VueKeys.webpackBinary := "./node_modules/.bin/webpack"
Assets / VueKeys.vuefy / VueKeys.webpackConfig := "./webpack.config.js"
