package utils

import cats.implicits._
import controllers.{TodoControllerError, JsonBodyParsingError, NoRequestBodyError}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.{AnyContent, Request}

object JsonParsingUtil {
  def parse[T](request: Request[AnyContent])(implicit reads: Reads[T]): Either[TodoControllerError, T] =
    Either.fromOption[TodoControllerError, JsValue](request.body.asJson, NoRequestBodyError)
      .map(Json.fromJson[T])
      .flatMap {
        case JsSuccess(value, _) => Either.right(value)
        case JsError(errors) => Either.left(JsonBodyParsingError(errors.mkString(", ")))
      }
}
