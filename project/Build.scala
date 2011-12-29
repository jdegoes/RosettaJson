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

  lazy val rosettaJson: Project = Project("rosetta-json", file(".")) settings(nexusSettings ++ Seq(
    organization := "com.reportgrid",
    version      := "0.3.5",
    scalaVersion := "2.9.1",
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    libraryDependencies ++= Seq(
      "com.reportgrid"          %% "blueeyes"            % "0.5.1",
      "net.databinder"          %% "dispatch-http-json"  % "0.8.5"   % "provided",
      "net.liftweb"             %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
      "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
      "org.scala-tools.testing" %% "specs"               % "1.6.9"   % "test"
    ),
    publishTo <<= (version) { version: String =>
      val nexus = "http://nexus.reportgrid.com/content/repositories/public-"
      if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
      else                                   Some("releases"  at nexus+"releases/")
    }
  ): _*)
}

// vim: set ts=4 sw=4 et:
