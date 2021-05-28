package controllers.todo

import daos.todo.TodoPayload
import play.api.libs.json.{Json, Reads}

case class UpdateTodoRequestBody(
  text: Option[String],
  isCompleted: Option[Boolean]
) {
  def toTodoPayload: TodoPayload = TodoPayload(text = text, isCompleted = isCompleted)
}

object UpdateTodoRequestBody {
  implicit val updateTodoRequestBodyReads: Reads[UpdateTodoRequestBody] = Json.reads
}