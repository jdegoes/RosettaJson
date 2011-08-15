import sbt._
import Keys._
import AltDependency._

object RosettaJsonBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.2"
  val buildScalaVersion = "2.9.1.RC2"
  
  val blueeyes = GitAltDependency(_: java.io.File, file("../blueeyes"), RootProject(uri("git://github.com/jdegoes/blueeyes")))

  override def projectDefinitions(base: File) = {
    val rosettaJson: Project = Project("rosetta-json", file(".")) settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      libraryDependencies ++= Seq(
        "net.databinder"          % "dispatch-http-json_2.9.0-1"  % "0.8.3"   % "provided",
        "net.liftweb"             % "lift-json_2.9.0-1"           % "2.4-M2"  % "provided" intransitive(),
        "org.scala-tools.testing" % "scalacheck_2.9.0-1"          % "1.9"     % "test",
        "org.scala-tools.testing" % "specs_2.9.0-1"               % "1.6.8"   % "test"
      )
    ) dependsOnAlt(blueeyes(base)) 
    
    rosettaJson :: Nil
  }
}


// vim: set ts=4 sw=4 et:
