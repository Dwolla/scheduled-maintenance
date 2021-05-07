package com.dwolla.scheduledmaintenance

import dev.holt.javatime.literals._
import io.circe.literal._
import io.circe.parser.parse

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class ScheduledMaintenanceResponseTest extends munit.FunSuite with FetchPolyfill {

  test("the response should be a 503 status code") {
    val output = Main.handleRequest()

    assert(output.status == 503)
    assert(output.statusText == "Service Unavailable (scheduled maintenance)")
  }

  test("the response should contain an appropriate JSON body") {
    val output = Main.handleRequest()

    output.text().toFuture.map { body =>
      assert(parse(body) == Right(
        json"""{
               "code": "ScheduledMaintenance",
               "message": "Services are temporarily unavailable while we perform scheduled maintenance"
             }"""))
    }
  }

  test("the response should contain an appropriate Retry-After header") {
    val expected = offsetDateTime"""2021-05-10T23:00:00-05:00""".toInstant

    val output = Main.handleRequest()

    val actual: Instant =
      output.headers
        .get("Retry-After")
        .map(DateTimeFormatter.RFC_1123_DATE_TIME.parse)
        .map(Instant.from)
        .get

    assert(expected == actual)
  }

}
