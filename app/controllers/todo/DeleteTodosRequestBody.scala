package controllers.todo

import daos.todo.TodoPayload
import play.api.libs.json.{Json, OFormat}

case class DeleteTodosRequestBody(
  isCompleted: Option[Boolean]
) {
  def toTodoPayload: TodoPayload = TodoPayload(isCompleted = isCompleted)
}

object DeleteTodosRequestBody {
  implicit val deleteTodosRequestBodyFormat: OFormat[DeleteTodosRequestBody] = Json.format
}
