package controllers.todo.requestbody

import daos.todo.TodoSelector
import play.api.libs.json.{Json, OFormat}

case class DeleteTodosRequestBody(
  isCompleted: Option[Boolean]
) {
  def toTodoSelector: TodoSelector = TodoSelector(isCompleted = isCompleted)
}

object DeleteTodosRequestBody {
  implicit val deleteTodosRequestBodyFormat: OFormat[DeleteTodosRequestBody] = Json.format
}
