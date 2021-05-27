package controllers

sealed abstract class ControllerError

case object NoRequestBodyError extends ControllerError

final case class JsonBodyParsingError(message: String) extends ControllerError

final case class ItemNotFoundByIdError(id: String) extends ControllerError

final case class InvalidIdFormatError(id: String) extends ControllerError

case object PersistenceLayerError extends ControllerError