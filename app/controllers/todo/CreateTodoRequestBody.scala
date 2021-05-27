package controllers.todo

import play.api.libs.json.{Json, Reads}

case class CreateTodoRequestBody(text: String)

object CreateTodoRequestBody {
  implicit val createTodoRequestBodyReads: Reads[CreateTodoRequestBody] = Json.reads
}