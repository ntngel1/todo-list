package controllers.todo

import controllers.common.{ControllerError, ServiceLayerError}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import services.todo.{DaoLayerError, TodoServiceError, UnableToCreateTodoWithEmptyTextError, UnableToFindTodoError, UnableToUpdateDeletedTodo}

object TodoServiceErrorToControllerErrorMapper extends (TodoServiceError => ControllerError) {
  override def apply(error: TodoServiceError): ControllerError = error match {
    case UnableToCreateTodoWithEmptyTextError => ServiceLayerError(BAD_REQUEST, error)
    case UnableToFindTodoError => ServiceLayerError(NOT_FOUND, error)
    case UnableToUpdateDeletedTodo => ServiceLayerError(BAD_REQUEST, error)
    case DaoLayerError(_) => ServiceLayerError(INTERNAL_SERVER_ERROR, error)
  }
}
