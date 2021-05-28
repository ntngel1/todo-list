package utils

import daos.InvalidIdError
import reactivemongo.bson.BSONObjectID

object BSONObjectIDUtil {
  def parseEither(id: String): Either[InvalidIdError, BSONObjectID] =
    BSONObjectID.parse(id)
      .map(Right(_))
      .recover { case throwable => Left(InvalidIdError(throwable.getMessage)) }
      .get
}
