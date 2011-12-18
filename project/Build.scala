import sbt._
import Keys._
import AltDependency._

object RosettaJsonBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.4"
  val buildScalaVersion = "2.9.1"
  
  val blueeyes = GitAltDependency(_: java.io.File, file("../blueeyes"), RootProject(uri("git://github.com/jdegoes/blueeyes")))

  override def projectDefinitions(base: File) = {
    val rosettaJson: Project = Project("rosetta-json", file(".")) settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      resolvers ++= Seq("ReportGrid repo" at            "http://devci01.reportgrid.com:8081/content/repositories/releases",
			"ReportGrid snapshot repo" at   "http://devci01.reportgrid.com:8081/content/repositories/snapshots"),
      libraryDependencies ++= Seq(
        "net.databinder"          %% "dispatch-http-json"  % "0.8.5"   % "provided",
        "net.liftweb"             %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
        "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
        "org.scala-tools.testing" %% "specs"               % "1.6.9"   % "test"
      ),
      publishTo <<= (version) { version: String =>
	val nexus = "http://nexus.reportgrid.com/content/repositories/"
	if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
	else                                   Some("releases"  at nexus+"releases/")
      },
      credentials := Credentials(Path.userHome / ".ivy2" / ".rgcredentials") :: Nil
    ) dependsOnAlt(blueeyes(base)) 
    
    rosettaJson :: Nil
  }
}


// vim: set ts=4 sw=4 et:
