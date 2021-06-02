package daos.todo

import models.TodoModel
import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.BSONObjectID
import utils.BSONObjectIDUtil
import reactivemongo.play.json._ // DO NOT REMOVE

case class TodoMongoModel(
  _id: BSONObjectID,
  text: String,
  isCompleted: Boolean,
  isDeleted: Boolean
) {
  def toTodoModel: TodoModel =
    TodoModel(
      id = _id.stringify,
      text = text,
      isCompleted = isCompleted
    )
}

object TodoMongoModel {
  def apply(todoModel: TodoModel): Either[InvalidIdError, TodoMongoModel] =
    BSONObjectIDUtil.parseEither(todoModel.id)
      .map { id =>
        TodoMongoModel(
          _id = id,
          text = todoModel.text,
          isCompleted = todoModel.isCompleted,
          isDeleted = false
        )
      }

  implicit val todoMongoModelFormat: OFormat[TodoMongoModel] = Json.format[TodoMongoModel]
}