package controllers.todo.requestbody

import play.api.libs.json.{Json, Reads}

case class UpdateTodoRequestBody(
  text: Option[String],
  isCompleted: Option[Boolean]
)

object UpdateTodoRequestBody {
  implicit val updateTodoRequestBodyReads: Reads[UpdateTodoRequestBody] = Json.reads
}