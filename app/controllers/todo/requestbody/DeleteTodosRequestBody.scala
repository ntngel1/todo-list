package controllers.todo.requestbody

import play.api.libs.json.{Json, OFormat}

case class DeleteTodosRequestBody(isCompleted: Boolean)

object DeleteTodosRequestBody {
  implicit val deleteTodosRequestBodyFormat: OFormat[DeleteTodosRequestBody] = Json.format
}
