import sbt._
import Keys._

object RosettaBuild extends Build {
  lazy val deps = Seq(
    "net.databinder"          %% "dispatch-http-json"  % "0.8.3"   % "provided",
    "net.liftweb"             %% "lift-json"           % "2.4-M2"  % "provided",
    "org.scala-tools.testing" %% "scalacheck"          % "1.9"     % "test",
    "org.scala-tools.testing" %% "specs"               % "1.6.8"   % "test"
  )

  def tryLocalGit(buildBase: java.io.File, p: Project, f: java.io.File, git: URI): Project = {
    val resolved = if (f.isAbsolute) f else new File(buildBase, f.getPath)
    val dep = if(resolved.isDirectory) RootProject(resolved) else RootProject(git)
    p dependsOn dep
  }

  override def projectDefinitions(base: File) = tryLocalGit(base, 
    Project("rosetta", file(".")) settings(name := "rosetta-json", organization := "com.reportgrid", libraryDependencies ++= deps, scalaVersion := "2.9.0-1"),
    file("../blueeyes"), 
    uri("https://github.com/jdegoes/blueeyes")) :: Nil
}
