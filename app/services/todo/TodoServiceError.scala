package services.todo

import daos.todo.TodoDaoError
import services.ServiceError

sealed abstract class TodoServiceError extends ServiceError

case object UnableToCreateTodoWithEmptyTextError extends TodoServiceError {
  override val errorCode: Int = 100
  override val errorMessage: String = "Unable to create todo with empty text"
}

case object UnableToMakeTodoTextEmptyError extends TodoServiceError {
  override val errorCode: Int = 107
  override val errorMessage: String = "Unable to make todo text empty"
}

case object UnableToDeleteAlreadyDeletedTodoError extends TodoServiceError {
  override val errorCode: Int = 106
  override val errorMessage: String = "Unable to delete todo that is already deleted"
}

final case class TodoWithSuchIdNotFoundError(id: String) extends TodoServiceError {
  override val errorCode: Int = 101
  override val errorMessage: String = s"Todo with id $id not found"
}

case object UnableToUpdateDeletedTodoError extends TodoServiceError {
  override val errorCode: Int = 103
  override val errorMessage: String = "Unable to update already deleted todo"
}

case object NoFieldsPassedToUpdateTodoError extends TodoServiceError {
  override val errorCode: Int = 105
  override val errorMessage: String = "No fields weren't passed to update Todo. Both isCompleted and text fields are null."
}

final case class InvalidTodoIdError(message: String) extends TodoServiceError {
  override val errorCode: Int = 104
  override val errorMessage: String = s"Invalid Todo ID: $message"
}

final case class DaoLayerError(error: TodoDaoError) extends TodoServiceError {
  override val errorCode: Int = 102
  override val errorMessage: String = s"Unknown error in DAO: ${error.errorMessage}"
}
