package utils

import daos.{DaoError, NotFoundError}
import play.api.mvc.Results._

object DaoErrorsMapper {
  implicit class DaoErrorHttpResult(error: DaoError) {

  }
}
