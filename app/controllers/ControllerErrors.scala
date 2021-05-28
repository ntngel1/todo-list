package controllers

import daos.UnknownDaoError

sealed abstract class ControllerError {
  val errorCode: Int
  val errorMessage: String
}

object NoRequestBodyError extends ControllerError {
  override val errorCode: Int = 1
  override val errorMessage: String = "No request body"
}

object ItemNotFoundError extends ControllerError {
  override val errorCode: Int = 2
  override val errorMessage: String = "Unable to find item"
}

final case class DatabaseError(reason: UnknownDaoError) extends ControllerError {
  override val errorCode: Int = 3
  override val errorMessage: String = s"Unknown database error. ${reason.message}"
}

case class JsonBodyParsingError(message: String) extends ControllerError {
  override val errorCode: Int = 4
  override val errorMessage: String = s"Error during parsing json from request body. $message"
}

case class InvalidIdFormatError(message: String) extends ControllerError {
  override val errorCode: Int = 5
  override val errorMessage: String = s"Invalid id format. $message"
}

