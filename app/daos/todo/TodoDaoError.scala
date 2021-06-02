package daos.todo

sealed abstract class TodoDaoError {
  val errorMessage: String
}

case object TodoNotFoundError extends TodoDaoError {
  override val errorMessage: String = "Unable to find todo in database"
}

case object CannotUpdateDeletedTodoError extends TodoDaoError {
  override val errorMessage: String = "Cannot update deleted todo"
}

final case class InvalidIdError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Invalid todo's id. $message"
}

final case class UnknownDaoError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Unknown error raised in database: $message"
}