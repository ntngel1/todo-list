package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._ // DO NOT REMOVE
import reactivemongo.bson.BSONObjectID

case class TodoModel(
  _id: Option[BSONObjectID],
  text: String,
  isCompleted: Boolean,
  isDeleted: Boolean
)

object TodoModel {
  implicit val jsonFormat: OFormat[TodoModel] = Json.format
}