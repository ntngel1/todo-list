package daos.todo

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._ // DO NOT REMOVE

case class TodoPayload(
  _id: Option[BSONObjectID] = Option.empty,
  text: Option[String] = Option.empty,
  isCompleted: Option[Boolean] = Option.empty,
  isDeleted: Option[Boolean] = Option.empty
)

object TodoPayload {
  implicit val todoPayloadFormat: OFormat[TodoPayload] = Json.format
}