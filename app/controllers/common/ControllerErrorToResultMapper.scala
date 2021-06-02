package controllers.common

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Status

object ControllerErrorToResultMapper extends (ControllerError => Result) {
  override def apply(error: ControllerError): Result = {
    val failResponse = Json.toJson(FailResponse(error))
    new Status(error.responseCode)(failResponse)
  }
}
