package controllers.todo

import daos.todo.TodoPayload
import play.api.libs.json.{Json, Reads}

case class UpdateTodosRequestBody(isCompleted: Option[Boolean]) {
  def toTodoPayload: TodoPayload = TodoPayload(isCompleted = isCompleted)
}

object UpdateTodosRequestBody {
  implicit val updateTodosRequestBody: Reads[UpdateTodosRequestBody] = Json.reads
}
