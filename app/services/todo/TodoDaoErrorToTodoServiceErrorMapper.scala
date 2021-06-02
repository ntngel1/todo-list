package services.todo

import daos.todo.{CannotDeleteAlreadyDeletedTodoError, CannotFindTodoWithSuchId, CannotUpdateDeletedTodoError, InvalidIdError, TodoDaoError, UnknownDaoError}

object TodoDaoErrorToTodoServiceErrorMapper extends (TodoDaoError => TodoServiceError) {
  override def apply(error: TodoDaoError): TodoServiceError = error match {
    case CannotFindTodoWithSuchId(id) => TodoWithSuchIdNotFoundError(id)
    case CannotUpdateDeletedTodoError => UnableToUpdateDeletedTodoError
    case CannotDeleteAlreadyDeletedTodoError => UnableToDeleteAlreadyDeletedTodoError
    case InvalidIdError(_) => InvalidTodoIdError(error.errorMessage)
    case UnknownDaoError(_) => DaoLayerError(error)
  }
}
