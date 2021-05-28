package utils

import play.api.mvc.Result
import play.api.mvc.Results._
import controllers.{ControllerError, DatabaseError, FailResponse, InvalidIdFormatError, ItemNotFoundError, JsonBodyParsingError, NoRequestBodyError}
import daos.{DaoError, InvalidIdError, NotFoundError, UnknownDaoError}
import play.api.libs.json.Json

object DaoErrorToControllerErrorMapper extends (DaoError => ControllerError) {
  override def apply(error: DaoError): ControllerError = error match {
    case NotFoundError => ItemNotFoundError
    case InvalidIdError(message) => InvalidIdFormatError(message)
    case UnknownDaoError(_) => DatabaseError(error.asInstanceOf[UnknownDaoError])
  }
}

object ControllerErrorToResultMapper extends (ControllerError => Result) {
  override def apply(error: ControllerError): Result = {
    val failResponse = Json.toJson(FailResponse(error))
    error match {
      case NoRequestBodyError => BadRequest(failResponse)
      case JsonBodyParsingError(_) => BadRequest(failResponse)
      case InvalidIdFormatError(_) => BadRequest(failResponse)
      case ItemNotFoundError => NotFound(failResponse)
      case DatabaseError(_) => InternalServerError(failResponse)
    }
  }
}

