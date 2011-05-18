import sbt._

class RosettaJsonProject(info: ProjectInfo) extends DefaultProject(info) with Repositories {
  val blueeyes      = "com.github.blueeyes"     %   "blueeyes"            % "0.2.8"   % "compile"
  val dispatch_json = "net.databinder"          %%  "dispatch-http-json"  % "0.7.8"   % "compile"
  val lift_json     = "net.liftweb"             %%  "lift-json"           % "2.3"     % "compile"

  val scala_check   = "org.scala-tools.testing" %% "scalacheck"   % "1.8"   % "test"
  val specs         = "org.scala-tools.testing" %% "specs"        % "1.6.7" % "compile"

  def scala_check_framework = new TestFramework("org.scalacheck.ScalaCheckFramework")

  override def testFrameworks = super.testFrameworks ++ Seq(scala_check_framework)
}

trait Repositories {
  val ScalaReleases   = MavenRepository("Scala Tools Releases",       "http://scala-tools.org/repo-releases/")
  val ScalaSnapshots  = MavenRepository("Scala Tools Snapshots",      "http://scala-tools.org/repo-snapshots/")
  val Sonatype        = MavenRepository("Sonatype Repository",        "http://oss.sonatype.org/content/repositories/releases/")
  val JBoss           = MavenRepository("JBoss Releases",             "http://repository.jboss.org/nexus/content/groups/public/")
  val Nexus           = MavenRepository("Nexus Scala Tools",          "http://nexus.scala-tools.org/content/repositories/releases/")
  val Maven           = MavenRepository("Maven Repo 1",               "http://repo1.maven.org/maven2/")
  val Scalable        = MavenRepository("Maven Repo 2",               "http://scalablesolutions.se/akka/repository/")
}