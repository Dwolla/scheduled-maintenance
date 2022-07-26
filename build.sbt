ThisBuild / organization := "com.dwolla"
ThisBuild / homepage := Some(url("https://github.com/Dwolla/scheduled-maintenance"))
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / developers := List(
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
)
ThisBuild / startYear := Option(2021)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("8"), JavaSpec.temurin("11"))
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq.empty
ThisBuild / githubWorkflowPublish := Seq.empty

lazy val `scalajs-stubs` = (project in file("stubs"))
  .settings(
    scalacOptions ~= { _.filterNot(Set(
      "-Wdead-code",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:imports",
      "-Wunused:locals",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:privates",
      "-Wvalue-discard",
    ).contains) },
  )
  .enablePlugins(ScalaJSPlugin)

lazy val `scheduled-maintenance` = (project in file("core"))
  .settings(
    description := "Cloudflare worker to return 503s from API endpoints during scheduled maintenance",
    libraryDependencies ++= {
      val circeV = "0.14.2"
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "2.2.0",
        "io.circe" %%% "circe-literal" % circeV,
        "dev.holt" %%% "java-time-literals" % "1.1.0",
        "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",
        "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.0",
        "org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0",
        "org.typelevel" %% "jawn-parser" % "1.0.0" % Compile,
        "org.scalameta" %%% "munit" % "0.7.25" % Test,
        "io.circe" %%% "circe-parser" % circeV % Test,
      )
    },
    scalaJSUseMainModuleInitializer := true,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Test / npmDependencies ++= Seq(
      "cross-fetch" -> "3.1.4",
    ),
  )
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(`scalajs-stubs`)

lazy val `scheduled-maintenance-root` = (project in file("."))
  .aggregate(`scheduled-maintenance`, `scalajs-stubs`)

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
