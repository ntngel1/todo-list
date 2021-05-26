package daos

sealed abstract class DaoError

final case class InvalidIdError(reason: Throwable) extends DaoError

final case class UnknownError(reason: Throwable) extends DaoError

case object NotFoundError extends DaoError