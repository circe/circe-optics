val Versions = new {
  val circe = "0.14.5"
  val monocle = "2.1.0"
  val discipline = "2.1.5"
  val scalaTestPlus = "3.2.11.0"

  val previousCirceOptics = "0.14.1"

  val scala212 = "2.12.17"
  val scala213 = "2.13.11"

  val scalaVersions = Seq(scala212, scala213)
}

ThisBuild / crossScalaVersions := Versions.scalaVersions
ThisBuild / scalaVersion := Versions.scala213
ThisBuild / tlFatalWarningsInCi := false //TODO: ... fix this someday

lazy val root = tlCrossRootProject.aggregate(optics)

lazy val optics = crossProject(JVMPlatform, JSPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("optics"))
  .settings(
    name := "circe-optics",
    description := "Monocle lenses and other tools for working with JSON values",
    mimaPreviousArtifacts := Set("io.circe" %% "circe-optics" % Versions.previousCirceOptics),
    libraryDependencies ++= Seq(
      "com.github.julien-truffaut" %%% "monocle-core" % Versions.monocle,
      "com.github.julien-truffaut" %%% "monocle-law" % Versions.monocle % Test,
      "io.circe" %%% "circe-core" % Versions.circe,
      "io.circe" %%% "circe-generic" % Versions.circe % Test,
      "io.circe" %%% "circe-testing" % Versions.circe % Test,
      "org.scalatestplus" %%% "scalacheck-1-15" % Versions.scalaTestPlus % Test,
      "org.typelevel" %%% "discipline-scalatest" % Versions.discipline % Test
    )
  )

ThisBuild / developers := List(
  Developer("travisbrown", "Travis Brown", "travisrobertbrown@gmail.com", url("https://twitter.com/travisbrown"))
)
