name := "rosetta-json"

version := "0.3.0"

organization := "com.reportgrid"

scalaVersion := "2.9.0-1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "com.reportgrid"          %% "blueeyes"            % "0.4.0"   % "provided",
  "net.databinder"          %% "dispatch-http-json"  % "0.8.3"   % "provided",
  "net.liftweb"             %% "lift-json"           % "2.4-M2"  % "provided",
  "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
  "org.scala-tools.testing" %% "specs"               % "1.6.8"   % "test"
)
