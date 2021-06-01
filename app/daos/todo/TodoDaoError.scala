package daos.todo

sealed abstract class TodoDaoError {
  val errorMessage: String
}

case object TodoNotFoundError extends TodoDaoError {
  override val errorMessage: String = "Unable to find todo in database"
}

final case class InvalidIdError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Invalid todo's id. $message"
}

final case class UnknownDaoError(message: String) extends TodoDaoError {
  override val errorMessage: String = s"Unknown error raised in database: $message"
}