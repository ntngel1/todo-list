package utils

import cats.implicits._
import controllers.common.{ControllerError, JsonBodyParsingError, NoRequestBodyError}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}

object JsonParsingUtil {
  def parse[T](jsValue: Option[JsValue])(implicit reads: Reads[T]): Either[ControllerError, T] =
    Either.fromOption[ControllerError, JsValue](jsValue, NoRequestBodyError)
      .map(Json.fromJson[T])
      .flatMap {
        case JsSuccess(value, _) => Either.right[ControllerError, T](value)
        case JsError(errors) => Either.left[ControllerError, T](JsonBodyParsingError(errors.mkString(", ")))
      }
}
