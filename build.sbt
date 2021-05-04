inThisBuild(List(
  organization := "com.dwolla",
  description := "Cloudflare worker to return 503s from API endpoints during scheduled maintenance",
  homepage := Some(url("https://github.com/Dwolla/scheduled-maintenance")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  scalaVersion := "2.13.5",
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
    scalaVersion := "2.13.5",
    libraryDependencies ++= {
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "1.0.0",
        "io.circe" %%% "circe-literal" % "0.13.0",
        "org.typelevel" %% "jawn-parser" % "1.0.0" % Compile,
      )
    },
    scalaJSUseMainModuleInitializer := true,
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`scalajs-stubs`)

lazy val `scheduled-maintenance-root` = (project in file("."))
  .aggregate(`scheduled-maintenance`, `scalajs-stubs`)

lazy val serverlessDeployCommand = settingKey[String]("serverless command to deploy the application")
serverlessDeployCommand := "serverless deploy --verbose"

lazy val deploy = taskKey[Int]("deploy to Cloudflare")
deploy := Def.task {
  import scala.sys.process._

  Process(
    serverlessDeployCommand.value,
    Option((`scheduled-maintenance-root` / baseDirectory).value),
    "ARTIFACT_PATH" -> (`scheduled-maintenance` / Compile / fullOptJS).value.toString,
  ).!
}.value
