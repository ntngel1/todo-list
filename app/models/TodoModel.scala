package models

import play.api.libs.json.{Json, OFormat}

case class TodoModel(
  id: String,
  text: String,
  isCompleted: Boolean
)

object TodoModel {
  implicit val jsonFormat: OFormat[TodoModel] = Json.format
}