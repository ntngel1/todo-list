package controllers.common

import play.api.libs.json.{JsNull, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.Ok

object ContentToResultMapper {
  implicit val unitWrites: Writes[Unit] = _ => JsNull

  def mapContentToResult[T](content: T)(implicit writes: Writes[T]): Result = {
    val response = Json.toJson(SuccessResponse(content))
    Ok(response)
  }
}
