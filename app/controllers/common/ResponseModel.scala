package controllers.common

import play.api.libs.json._

abstract class ResponseModel(val ok: Boolean)

case class SuccessResponse[T](
  content: T
) extends ResponseModel(ok = true)

case class FailResponse(
  errorCode: Int,
  errorMessage: String
) extends ResponseModel(ok = false)

object SuccessResponse {
  implicit def successResponseWrites[T](implicit writes: Writes[T]): Writes[SuccessResponse[T]] =
    (response: SuccessResponse[T]) => JsObject(
      Seq(
        "ok" -> JsBoolean(response.ok),
        "content" -> Json.toJson(response.content)
      )
    )
}

object FailResponse {
  def apply(error: ControllerError): FailResponse = new FailResponse(error.errorCode, error.errorMessage)

  implicit def failResponseWrites: Writes[FailResponse] = (response: FailResponse) => JsObject(
    Seq(
      "ok" -> JsBoolean(response.ok),
      "errorCode" -> JsNumber(response.errorCode),
      "errorMessage" -> JsString(response.errorMessage)
    )
  )
}