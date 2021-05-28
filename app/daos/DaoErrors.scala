package daos

sealed abstract class DaoError

case object NotFoundError extends DaoError

final case class InvalidIdError(message: String) extends DaoError

final case class UnknownDaoError(message: String) extends DaoError