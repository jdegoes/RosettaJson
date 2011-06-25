import sbt._
import Keys._
import AltDependency._

object RosettaJsonBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.1"
  val buildScalaVersion = "2.9.0-1"
  
  val blueeyes = GitAltDependency(_: java.io.File, file("../../blueeyes"),     RootProject(uri("git://github.com/jdegoes/blueeyes")))

  override def projectDefinitions(base: File) = {
    val rosettaJson: Project = Project("rosetta-json", file(".")) settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      libraryDependencies ++= Seq(
        "net.databinder"          %% "dispatch-http-json"  % "0.8.3"   % "provided",
        "net.liftweb"             %% "lift-json"           % "2.4-M2"  % "provided",
        "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
        "org.scala-tools.testing" %% "specs"               % "1.6.8"   % "test"
      )
    ) dependsOnAlt(blueeyes(base)) 
    
    rosettaJson :: Nil
  }
}


// vim: set ts=4 sw=4 et:
