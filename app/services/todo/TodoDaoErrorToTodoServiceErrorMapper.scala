package services.todo

import daos.todo.{InvalidIdError, TodoDaoError, TodoNotFoundError, UnknownDaoError}

object TodoDaoErrorToTodoServiceErrorMapper extends (TodoDaoError => TodoServiceError) {
  override def apply(error: TodoDaoError): TodoServiceError = error match {
    case TodoNotFoundError => UnableToFindTodoError
    case InvalidIdError(_) => DaoLayerError(error)
    case UnknownDaoError(_) => DaoLayerError(error)
  }
}
