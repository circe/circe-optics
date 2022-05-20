import sbtcrossproject.{ CrossType, crossProject }

ThisBuild / organization := "io.circe"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

val circeVersion = "0.14.2"
val monocleVersion = "2.1.0"
val previousCirceOpticsVersion = "0.11.0"

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

ThisBuild / crossScalaVersions := Seq("2.12.15", "2.13.7")

val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions ++= (
    if (priorTo2_13(scalaVersion.value))
      Seq(
        "-Xfuture",
        "-Yno-adapted-args",
        "-Ywarn-unused-import"
      )
    else
      Seq(
        "-Ywarn-unused:imports"
      )
  ),
  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  Test / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  coverageHighlighting := true,
  coverageEnabled := (
    if (priorTo2_13(scalaVersion.value)) false else coverageEnabled.value
  ),
  Compile / scalastyleSources ++= (Compile / unmanagedSourceDirectories).value
)

val allSettings = baseSettings ++ publishSettings

val docMappingsApiDir = settingKey[String]("Subdirectory in site target directory for API docs")

val root = project.in(file(".")).settings(allSettings).settings(noPublishSettings).aggregate(opticsJVM, opticsJS)

lazy val optics = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("optics"))
  .settings(allSettings)
  .settings(
    moduleName := "circe-optics",
    mimaPreviousArtifacts := Set("io.circe" %% "circe-optics" % previousCirceOpticsVersion),
    libraryDependencies ++= Seq(
      "com.github.julien-truffaut" %%% "monocle-core" % monocleVersion,
      "com.github.julien-truffaut" %%% "monocle-law" % monocleVersion % Test,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "io.circe" %%% "circe-testing" % circeVersion % Test,
      "org.scalatestplus" %%% "scalacheck-1-14" % "3.2.2.0" % Test,
      "org.typelevel" %%% "discipline-scalatest" % "2.1.5" % Test
    ),
    ghpagesNoJekyll := true,
    docMappingsApiDir := "api",
    addMappingsToSiteDir(Compile / packageDoc / mappings, docMappingsApiDir)
  )
  .jsSettings(
    libraryDependencies +=
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0" % Test,
    coverageEnabled := false
  )

lazy val opticsJVM = optics.jvm
lazy val opticsJS = optics.js

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVcsSign := true,
  homepage := Some(url("https://github.com/circe/circe-optics")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  autoAPIMappings := true,
  apiURL := Some(url("https://circe.github.io/circe-optics/api/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/circe/circe-optics"),
      "scm:git:git@github.com:circe/circe-optics.git"
    )
  ),
  developers := List(
    Developer(
      "travisbrown",
      "Travis Brown",
      "travisrobertbrown@gmail.com",
      url("https://twitter.com/travisbrown")
    )
  )
)

ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.8")
// No auto-publish atm. Remove this line to generate publish stage
ThisBuild / githubWorkflowPublishTargetBranches := Seq.empty
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("clean", "coverage", "test", "coverageReport", "scalastyle", "scalafmtCheckAll"),
    id = None,
    name = Some("Test")
  ),
  WorkflowStep.Use(
    UseRef.Public(
      "codecov",
      "codecov-action",
      "v1"
    )
  )
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
).toSeq
