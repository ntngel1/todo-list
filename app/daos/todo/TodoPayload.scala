package daos.todo

import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._ // DO NOT REMOVE
import reactivemongo.bson.BSONObjectID

case class TodoPayload(
  _id: Option[BSONObjectID] = Option.empty,
  text: Option[String] = Option.empty,
  isCompleted: Option[Boolean] = Option.empty,
  isDeleted: Option[Boolean] = Option.empty
)

object TodoPayload {
  implicit val todoPayloadFormat: OFormat[TodoPayload] = Json.format
}