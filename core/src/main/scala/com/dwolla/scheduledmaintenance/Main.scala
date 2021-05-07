package com.dwolla.scheduledmaintenance

import dev.holt.javatime.literals.offsetDateTime
import io.circe.literal._
import org.scalajs.dom.experimental._
import org.scalajs.dom.experimental.serviceworkers.FetchEvent
import stubs.Globals

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset}
import scala.scalajs.js

object Main {
  def main(args: Array[String]): Unit =
    Globals.addEventListener("fetch", (_: FetchEvent).respondWith(handleRequest()))

  //noinspection SameParameterValue
  private def formatForHttpHeader(odt: OffsetDateTime): String =
    odt.toInstant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)

  private[scheduledmaintenance] def handleRequest(): Response =
    new Response(
      json"""{
               "code": "ScheduledMaintenance",
               "message": "Services are temporarily unavailable while we perform scheduled maintenance"
             }""".noSpaces,
      ResponseInit(
        _status = 503,
        _statusText = "Service Unavailable (scheduled maintenance)",
        _headers = js.Dictionary(
          "content-type" -> "application/json",
          "Retry-After" -> formatForHttpHeader(offsetDateTime"""2021-05-10T23:00:00-05:00"""),
        )))
}
