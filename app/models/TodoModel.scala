package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONHandler, BSONObjectID, Macros}

case class TodoModel(
  _id: Option[BSONObjectID],
  text: String,
  isCompleted: Boolean,
  isDeleted: Boolean
)

object TodoModel {
  //implicit val jsonFormat: OFormat[TodoModel] = Json.format
  implicit val bsonHandler: BSONHandler[TodoModel] = Macros.handler
}