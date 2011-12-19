import sbt._
import Keys._

object RosettaJsonBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.4"
  val buildScalaVersion = "2.9.1"

  val blueeyesDeps = com.samskivert.condep.Depends( 
    ("blueeyes",    null, "com.reportgrid" %% "blueeyes"     % "0.5.0-SNAPSHOT")
  )  

  override def projectDefinitions(base: File) = {
    val rosettaJson: Project = blueeyesDeps.addDeps(Project("rosetta-json", file(".")).settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      libraryDependencies ++= (blueeyesDeps.libDeps ++ Seq(
        "net.databinder"          %% "dispatch-http-json"  % "0.8.5"   % "provided",
        "net.liftweb"             %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
        "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
        "org.scala-tools.testing" %% "specs"               % "1.6.9"   % "test"
      )),
      resolvers ++= Seq("ReportGrid repo" at                   "http://nexus.reportgrid.com/content/repositories/releases",
			"ReportGrid snapshot repo" at          "http://nexus.reportgrid.com/content/repositories/snapshots",
			"ReportGrid public repo" at            "http://nexus.reportgrid.com/content/repositories/public-releases",
			"ReportGrid public snapshot repo" at   "http://nexus.reportgrid.com/content/repositories/public-snapshots"),
      publishTo <<= (version) { version: String =>
        val nexus = "http://nexus.reportgrid.com/content/repositories/"
        if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
        else                                   Some("releases"  at nexus+"releases/")
      },
      credentials += Credentials(Path.userHome / ".ivy2" / ".rgcredentials")
    ))
    
    rosettaJson :: Nil
  }
}


// vim: set ts=4 sw=4 et:
