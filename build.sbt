val Versions = new {
  val circe = "0.14.13"
  val monocle = "3.3.0"
  val discipline = "2.3.0"
  val scalaTestPlus = "3.2.19.0"

  val scala213 = "2.13.15"
  val scala3 = "3.3.4"

  val scalaVersions = Seq(scala213, scala3)
}

ThisBuild / crossScalaVersions := Versions.scalaVersions
ThisBuild / scalaVersion := Versions.scala213
ThisBuild / tlFatalWarnings := false //TODO: ... fix this someday
ThisBuild / startYear := Some(2016)

ThisBuild / tlBaseVersion := "0.15"

val semVerRegex = """(\d+\.\d+\.)(\d+)(?:-SNAPSHOT)?""".r

lazy val root = tlCrossRootProject.aggregate(optics)

lazy val optics = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("optics"))
  .settings(
    name := "circe-optics",
    description := "Monocle lenses and other tools for working with JSON values",
    libraryDependencies ++= Seq(
      "dev.optics" %%% "monocle-core" % Versions.monocle,
      "dev.optics" %%% "monocle-macro" % Versions.monocle % Test,
      "dev.optics" %%% "monocle-law" % Versions.monocle % Test,
      "io.circe" %%% "circe-core" % Versions.circe,
      "io.circe" %%% "circe-generic" % Versions.circe % Test,
      "io.circe" %%% "circe-testing" % Versions.circe % Test,
      "org.scalatestplus" %%% "scalacheck-1-18" % Versions.scalaTestPlus % Test,
      "org.typelevel" %%% "discipline-scalatest" % Versions.discipline % Test
    )
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "0.15.1").toMap
  )

ThisBuild / developers := List(
  Developer("travisbrown", "Travis Brown", "travisrobertbrown@gmail.com", url("https://twitter.com/travisbrown"))
)
