import sbt._
object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn (altDep)
  lazy val altDep = RootProject(uri("git://github.com/reportgrid/xsbt-alt-deps")) 
}

// vim: set ts=4 sw=4 et:
