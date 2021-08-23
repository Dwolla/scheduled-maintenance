inThisBuild(List(
  organization := "com.dwolla",
  description := "Cloudflare worker to return 503s from API endpoints during scheduled maintenance",
  homepage := Some(url("https://github.com/Dwolla/scheduled-maintenance")),
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  scalaVersion := "2.13.6",
  developers := List(
    Developer(
      "bpholt",
      "Brian Holt",
      "bholt+scheduled-maintenance@dwolla.com",
      url("https://dwolla.com")
    ),
    Developer(
      "benpjackson",
      "Ben Jackson",
      "bjackson+scheduled-maintenance@dwolla.com",
      url("https://dwolla.com")
    ),
  ),
  startYear := Option(2021),

  githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11"),
  githubWorkflowTargetTags ++= Seq("v*"),
  githubWorkflowPublishTargetBranches := Seq.empty,
  githubWorkflowPublish := Seq.empty,
))

lazy val `scheduled-maintenance` = (project in file("core"))
  .settings(
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full),
    libraryDependencies ++= {
      val circeV = "0.14.1"
      val http4sV = "1.0.0-M24"
      Seq(
        "org.typelevel" %%% "feral-cloudflare-worker" % "0.1-dc58235",
        "org.http4s" %%% "http4s-client" % http4sV,
        "org.http4s" %%% "http4s-core" % http4sV,
        "org.http4s" %%% "http4s-jawn" % http4sV,
        "io.circe" %%% "circe-literal" % circeV,
        "dev.holt" %%% "java-time-literals" % "1.0.0",
        "org.typelevel" %% "jawn-parser" % "1.0.0" % Compile,
        "org.scalameta" %%% "munit" % "0.7.28" % Test,
        "org.typelevel" %%% "munit-cats-effect-3" % "1.0.5" % Test,
        "io.circe" %%% "circe-parser" % circeV % Test,
        "org.http4s" %%% "http4s-circe" % "1.0.0-M24" % Test,
      )
    },
    scalaJSUseMainModuleInitializer := true,
    Test / parallelExecution := false,
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scheduled-maintenance-root` = (project in file("."))
  .aggregate(`scheduled-maintenance`)

lazy val serverlessDeployCommand = settingKey[String]("serverless command to deploy the application")
serverlessDeployCommand := "serverless deploy --verbose"

lazy val deploy = taskKey[Int]("deploy to Cloudflare")
deploy := Def.task {
  import scala.sys.process._

  val exitCode = Process(
    serverlessDeployCommand.value,
    Option((`scheduled-maintenance-root` / baseDirectory).value),
    "ARTIFACT_PATH" -> (`scheduled-maintenance` / Compile / fullOptJS).value.data.toString,
  ).!

  if (exitCode == 0) exitCode
  else throw new IllegalStateException("Serverless returned a non-zero exit code. Please check the logs for more information.")
}.value
