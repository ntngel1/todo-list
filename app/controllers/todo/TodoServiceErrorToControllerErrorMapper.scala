package controllers.todo

import controllers.common.{ControllerError, ServiceLayerError}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import services.todo.{DaoLayerError, TodoServiceError, TodoWithSuchIdNotFoundError}

object TodoServiceErrorToControllerErrorMapper extends (TodoServiceError => ControllerError) {
  override def apply(error: TodoServiceError): ControllerError = error match {
    case TodoWithSuchIdNotFoundError(_) => ServiceLayerError(NOT_FOUND, error)
    case DaoLayerError(_) => ServiceLayerError(INTERNAL_SERVER_ERROR, error)
    case _ => ServiceLayerError(BAD_REQUEST, error)
  }
}
