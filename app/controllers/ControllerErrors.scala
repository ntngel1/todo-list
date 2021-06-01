package controllers

import play.api.http.Status._
import services.todo.TodoServiceError

sealed abstract class TodoControllerError {
  val errorCode: Int
  val responseCode: Int
  val errorMessage: String
}

case object NoRequestBodyError extends TodoControllerError {
  override val errorCode: Int = 1
  override val responseCode: Int = BAD_REQUEST
  override val errorMessage: String = "No request body"
}

final case class JsonBodyParsingError(message: String) extends TodoControllerError {
  override val errorCode: Int = 4
  override val responseCode: Int = BAD_REQUEST
  override val errorMessage: String = s"Error during parsing json from request body. $message"
}

final case class ServiceLayerError(override val responseCode: Int, reason: TodoServiceError) extends TodoControllerError {
  override val errorCode: Int = reason.errorCode
  override val errorMessage: String = reason.errorMessage
}