package models

import actors.urlActor.{ShortenUrlRequest, ShortenUrlResponse}
import akka.http.scaladsl.model._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import java.time.ZonedDateTime

trait JsonFormats extends DefaultJsonProtocol {
  import java.time.format.DateTimeFormatter

  // JSON formatter for ZonedDateTime
  implicit object ZonedDateTimeJsonFormat extends JsonFormat[ZonedDateTime] {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    def write(zdt: ZonedDateTime): JsValue = JsString(zdt.format(formatter))

    def read(value: JsValue): ZonedDateTime = value match {
      case JsString(str) => ZonedDateTime.parse(str, formatter)
      case _             => throw DeserializationException("Expected ZonedDateTime as JsString")
    }
  }

  // Your existing formatters
  implicit val urlRequest = jsonFormat2(ShortenUrlRequest)
  implicit val urlResponse = jsonFormat6(ShortenUrlResponse)

  def JsonResponse(body: String): HttpResponse = {
    HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        body
      )
    )
  }
}
