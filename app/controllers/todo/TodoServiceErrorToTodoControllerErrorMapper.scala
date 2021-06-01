package controllers.todo

import controllers.{ServiceLayerError, TodoControllerError}
import play.api.http.Status._
import services.todo.{DaoLayerError, TodoServiceError, UnableToCreateTodoWithEmptyTextError, UnableToFindTodoError}

object TodoServiceErrorToTodoControllerErrorMapper extends (TodoServiceError => TodoControllerError){

  override def apply(error: TodoServiceError): TodoControllerError = error match {
    case UnableToCreateTodoWithEmptyTextError => ServiceLayerError(BAD_REQUEST, error)
    case UnableToFindTodoError => ServiceLayerError(NOT_FOUND, error)
    case DaoLayerError(_) => ServiceLayerError(INTERNAL_SERVER_ERROR, error)
  }
}
