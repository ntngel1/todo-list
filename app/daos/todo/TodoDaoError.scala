package daos.todo

sealed abstract class TodoDaoError {
  val errorMessage: String
}

final case class CannotFindTodoWithSuchId(id: String) extends TodoDaoError {
  override val errorMessage: String = s"Unable to find todo with id=$id"
}

case object CannotUpdateDeletedTodoError extends TodoDaoError {
  override val errorMessage: String = "Cannot update deleted todo"
}

case object CannotDeleteAlreadyDeletedTodoError extends TodoDaoError {
  override val errorMessage: String = "Cannot delete already deleted todo"
}

final case class InvalidIdError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Invalid todo id. $message"
}

final case class UnknownDaoError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Unknown error raised in database: $message"
}