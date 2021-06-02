package services.todo

import daos.todo.TodoDaoError
import services.ServiceError

sealed abstract class TodoServiceError extends ServiceError

case object UnableToCreateTodoWithEmptyTextError extends TodoServiceError {
  override val errorCode: Int = 100
  override val errorMessage: String = "Unable to create todo with empty text"
}

case object UnableToFindTodoError extends TodoServiceError {
  override val errorCode: Int = 101
  override val errorMessage: String = "Unable to find todo"
}

case object UnableToUpdateDeletedTodo extends TodoServiceError {
  override val errorCode: Int = 103
  override val errorMessage: String = "Unable to update already deleted todo"
}

final case class DaoLayerError(error: TodoDaoError) extends TodoServiceError {
  override val errorCode: Int = 102
  override val errorMessage: String = s"Unknown error in DAO: ${error.errorMessage}"
}
