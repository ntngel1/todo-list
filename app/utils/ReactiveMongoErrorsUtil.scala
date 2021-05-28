package utils

import reactivemongo.api.commands.{WriteError, WriteResult}

object ReactiveMongoErrorsUtil {

  private def formatWriteErrors(errors: Iterable[WriteError]): String = {
    errors.map { error => s"Error №${error.index}, ErrorCode ${error.code}: ${error.errmsg}" }
      .mkString("\n")
  }

  implicit class WriteResultErrorMessage(writeResult: WriteResult) {
    def formattedErrorMessage: String = formatWriteErrors(writeResult.writeErrors)
  }
}
