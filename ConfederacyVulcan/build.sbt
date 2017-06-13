name := "Confederacy of Vulcan"

version := "0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayJava, DebianPlugin)

maintainer in Linux := "Carlin Robertson <carlin.robertson.colin@googlemail.com>"

packageSummary in Linux := "Confederacy of Vulcan"

packageDescription := "Confederacy Web Application"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.postgresql" % "postgresql" % "42.1.1"
)
