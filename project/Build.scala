import sbt._
import Keys._

object RosettaJsonBuild extends Build {
  val nexusSettings = Defaults.defaultSettings ++ Seq (
    resolvers ++= Seq("ReportGrid repo"          at   "http://nexus.reportgrid.com/content/repositories/releases",
                      "ReportGrid repo (public)" at   "http://nexus.reportgrid.com/content/repositories/public-releases",
                      "ReportGrid snapshot repo"          at   "http://nexus.reportgrid.com/content/repositories/snapshots",
                      "ReportGrid snapshot repo (public)" at   "http://nexus.reportgrid.com/content/repositories/public-snapshots"),

    credentials += Credentials(Path.userHome / ".ivy2" / ".rgcredentials")
  )

  val blueeyesDeps = com.samskivert.condep.Depends( 
    ("blueeyes",    null, "com.reportgrid" %% "blueeyes"     % "0.5.1" % "provided")
  )  

  lazy val rosettaJson: Project = blueeyesDeps addDeps {
    Project(id = "rosetta-json", base = file(".")) settings(nexusSettings ++ Seq(
      organization := "com.reportgrid",
      version      := "0.3.5",
      scalaVersion := "2.9.1",
      scalacOptions ++= Seq("-deprecation", "-unchecked"),

      libraryDependencies ++= blueeyesDeps.libDeps ++ Seq(
        "joda-time"               %  "joda-time"           % "1.6.2"   % "provided",
        "net.databinder"          %% "dispatch-http-json"  % "0.8.5"   % "provided",
        "net.liftweb"             %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
        "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
        "org.scala-tools.testing" %% "specs"               % "1.6.9"   % "test"),

      publishTo <<= (version) { version: String =>
        val nexus = "http://nexus.reportgrid.com/content/repositories/"
        if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"public-snapshots/") 
        else                                   Some("releases"  at nexus+"public-releases/")
      }
    ): _*)
  }
}

// vim: set ts=4 sw=4 et:
