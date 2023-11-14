package com.dwolla.scheduledmaintenance

import dev.holt.javatime.literals.offsetDateTime
import io.circe.literal._
import org.scalajs.dom._
import stubs.Globals

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset}
import scala.annotation.nowarn
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

object Main {
  @nowarn("cat=other")
  def main(args: Array[String]): Unit =
    Globals.addEventListener("fetch",
      (event: FetchEvent) => event.respondWith(handleRequest(event.request).toJSPromise)
    )

  //noinspection SameParameterValue
  private def formatForHttpHeader(odt: OffsetDateTime): String =
    odt.toInstant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)

  private[scheduledmaintenance] def handleRequest(req: Request): Future[Response] = {
    if(Option(req.headers.get("Access")).exists(_.contains("733a068a-4be5-4ce6-9ef3-67205de0f447")))
      return fetch(req).toFuture

    if (Option(req.headers.get("Accept")).exists(_.contains("text/html")))
      buildResponse("text/html",
        """<!doctype html>
          |<html lang="en-US">
          |
          |<head>
          |  <meta charset="utf-8">
          |  <meta http-equiv="x-ua-compatible" content="ie=edge">
          |  <title>Scheduled Maintenance Underway | Dwolla</title>
          |  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
          |
          |  <link href="https://fonts.googleapis.com/css?family=Poppins:300,500|Roboto:300" rel="stylesheet">
          |
          |  <style type="text/css">
          |    h1, h2 {
          |      font-family: Poppins, sans-serif;
          |    }
          |
          |    h1 {
          |      font-weight: 500;
          |      line-height: 120px;
          |      font-size: 110px;
          |      margin: 0 0 30px;
          |      color: #2a2d38;
          |    }
          |
          |    h2 {
          |      font-weight: 300;
          |      line-height: 54px;
          |      font-size: 40px;
          |      margin: 0 0 35px;
          |      color: #2b334e;
          |    }
          |
          |    p {
          |      margin: 0;
          |    }
          |
          |    p a {
          |      color: #ee874b;
          |      text-decoration: none;
          |    }
          |
          |    .container {
          |      text-align: center;
          |    }
          |
          |    .logo {
          |      width: 169px;
          |      margin: 35px 0 100px;
          |    }
          |
          |    .short-desc {
          |      max-width: 600px;
          |      margin: 0 auto;
          |      font-weight: 300;
          |      line-height: 35px;
          |      font-size: 23px;
          |      font-family: Roboto, sans-serif;
          |      color: #6a7282;
          |    }
          |
          |  @media only screen and (max-width: 768px) {
          |    h1 {
          |      line-height: 70px;
          |      font-size: 50px;
          |      margin-bottom: 20px;
          |    }
          |
          |    h2 {
          |      margin-bottom: 20px;
          |      line-height: 28px;
          |      font-size: 18px;
          |    }
          |
          |    .container{
          |      margin: 0 30px;
          |    }
          |
          |    .logo {
          |      margin-bottom: 80px;
          |    }
          |
          |    .short-desc {
          |      max-width: 430px;
          |      line-height: 30px;
          |      font-size: 16px;
          |    }
          |  }
          |  </style>
          |</head>
          |
          |<body>
          |  <section class="container">
          |    <a href="https://status.dwolla.com">
          |      <img src="https://cdn.dwolla.com/com/dist/images/global/dwolla-logo-full-color_8fa10429.svg" class="logo" alt="Dwolla">
          |    </a>
          |    <h1>503</h1>
          |    <h2>Service Unavailable: Scheduled Maintenance</h2>
          |    <div class="short-desc">
          |      <p>
          |        Services are temporarily unavailable while we perform scheduled
          |        maintenance. See <a href="https://status.dwolla.com">status.dwolla.com</a>
          |        for more information.
          |      </p>
          |    </div>
          |  </section>
          |</body>
          |
          |</html>
          |""".stripMargin)
    else
      buildResponse("application/json",
        json"""{
                 "code": "ScheduledMaintenance",
                 "message": "Services are temporarily unavailable while we perform scheduled maintenance"
               }""".noSpaces)
  }

  private def buildResponse(contentType: String, body: String): Future[Response] = {
    Future.successful(new Response(
      body,
      new ResponseInit {
        status = 503
        statusText = "Service Unavailable (scheduled maintenance)"
        headers = js.Dictionary(
          "content-type" -> contentType,
          "Retry-After" -> formatForHttpHeader(offsetDateTime"""2023-11-18T11:00:00-06:00"""),
        )
      }
    )
    )
  }
}
