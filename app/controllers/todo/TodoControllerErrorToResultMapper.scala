package controllers.todo

import controllers.{FailResponse, TodoControllerError}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._

object TodoControllerErrorToResultMapper extends (TodoControllerError => Result) {
  override def apply(error: TodoControllerError): Result = {
    val failResponse = Json.toJson(FailResponse(error))
    new Status(error.responseCode)(failResponse)
  }
}

