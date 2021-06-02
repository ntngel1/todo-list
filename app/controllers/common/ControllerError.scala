package controllers.common

import play.api.http.Status.BAD_REQUEST
import services.ServiceError

sealed abstract class ControllerError {
  val errorCode: Int
  val responseCode: Int
  val errorMessage: String
}

case object NoRequestBodyError extends ControllerError {
  override val errorCode: Int = 1
  override val responseCode: Int = BAD_REQUEST
  override val errorMessage: String = "No request body"
}

final case class JsonBodyParsingError(message: String) extends ControllerError {
  override val errorCode: Int = 4
  override val responseCode: Int = BAD_REQUEST
  override val errorMessage: String = s"Error during parsing json from request body. $message"
}

final case class ServiceLayerError(override val responseCode: Int, error: ServiceError) extends ControllerError {
  override val errorCode: Int = error.errorCode
  override val errorMessage: String = error.errorMessage
}