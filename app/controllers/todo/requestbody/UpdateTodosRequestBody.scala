package controllers.todo.requestbody

import play.api.libs.json.{Json, Reads}

case class UpdateTodosRequestBody(isCompleted: Boolean)

object UpdateTodosRequestBody {
  implicit val updateTodosRequestBody: Reads[UpdateTodosRequestBody] = Json.reads
}
