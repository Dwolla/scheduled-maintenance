package com.dwolla.scheduledmaintenance

import cats.effect._
import dev.holt.javatime.literals._
import io.circe.Json
import io.circe.literal._
import munit.CatsEffectSuite
import org.http4s.EntityDecoder
import org.http4s.Method.GET
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers._
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}

class ScheduledMaintenanceResponseTest
  extends CatsEffectSuite
    with Http4sClientDsl[IO] {

  private def defaultRequest =
    GET(uri"https://hydragents.xyz/test")

  private def client = Main.routes.map(r => Client.fromHttpApp[IO](r.orNotFound))

  test("the response should be a 503 status code") {
    client.flatMap(_.run(defaultRequest))
      .use { output => IO {
        assertEquals(output.status.code, 503)
        assertEquals(output.status.reason, "Service Unavailable (scheduled maintenance)")
      }}
  }

  test("the response should contain an appropriate JSON body") {
    val expectedJson =
      json"""{
               "code": "ScheduledMaintenance",
               "message": "Services are temporarily unavailable while we perform scheduled maintenance"
             }"""

    client.flatMap(_.run(defaultRequest))
      .evalMap {
        EntityDecoder[IO, Json]
          .decode(_, strict = true)
          .value
      }
      .use { output => IO {
        assertEquals(output, Right(expectedJson))
      }}
  }

  test("the response should contain an appropriate Retry-After header") {
    client.flatMap(_.run(defaultRequest))
      .map(_.headers.get[`Retry-After`])
      .use { output => IO {
        assertEquals(output, Option(`Retry-After`.unsafeFromLong(offsetDateTime"""2021-05-16T00:00:00-05:00""".toEpochSecond)))
      }}
  }

  test("if the request asks for HTML, give it HTML") {
    val accept = Accept.parse("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").fold(throw _, identity)
    val req = GET(uri"https://hydragents.xyz/test", accept)

    client.flatMap(_.run(req))
      .use { output => IO {
        assertEquals(output.status.code, 503)
        assertEquals(output.headers.get[`Content-Type`], Option(`Content-Type`(mediaType"text/html")))
      }}
  }
}
