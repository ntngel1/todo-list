package utils

import controllers.SuccessResponse
import play.api.libs.json.{JsNull, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.Ok

object ContentToResultMappingUtil {
  implicit val unitWrites: Writes[Unit] = (_: Unit) => JsNull

  def map[T](content: T)(implicit writes: Writes[T]): Result = Ok(Json.toJson(SuccessResponse(content)))
}
