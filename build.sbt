name := "play-scala-anorm-example"

version := "2.6.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  jdbc ,
  evolutions ,
  guice ,
  ehcache ,
  ws ,
  "com.h2database" % "h2" % "1.4.196",
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.amazonaws" % "aws-java-sdk" % "1.11.221",
  "com.sksamuel.scrimage" % "scrimage-core_2.12" % "2.1.8",
  //"com.sksamuel.scrimage" % "scrimage-core_2.12.0-M3" % "2.1.7",
  "org.playframework.anorm" %% "anorm" % "2.6.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test ,

  specs2 % Test,

  "org.webjars" %% "webjars-play" % "2.6.3",
  "org.webjars" % "bootstrap" % "4.0.0-2",
  "org.webjars" % "jquery" % "3.3.1-1",
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.5" % "test",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.5" % "test",
  "de.svenkubiak" % "jBCrypt" % "0.4.1"

 )