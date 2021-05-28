package daos.todo

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._
import daos._
import models.TodoModel
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.BSONObjectIDUtil
import utils.ReactiveMongoErrorsUtil._

class TodoDao @Inject()(
  implicit executionContext: ExecutionContext,
  val reactiveMongoApi: ReactiveMongoApi
) {

  private def todos =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("todos"))

  def getAllTodos: EitherT[Future, DaoError, List[TodoModel]] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos =>
        todos.find(TodoPayload(isDeleted = Option(false)), Option.empty[JsObject])
          .cursor[TodoMongoModel]()
          .collect[List](-1, Cursor.FailOnError[List[TodoMongoModel]]())
      }
      .map { todoMongoModels =>
        todoMongoModels.map(_.toTodoModel)
      }
  }

  def getTodoById(id: String): EitherT[Future, DaoError, TodoModel] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[DaoError]
      .flatMap { objectId =>
        EitherT.right[DaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoPayload(_id = Option(objectId), isDeleted = Option(false)), Option.empty[JsObject])
              .cursor[TodoMongoModel]()
              .headOption
          }
      }
      .flatMap { todoMongoModel =>
        EitherT.fromOption[Future](todoMongoModel, NotFoundError)
          .leftWiden[DaoError]
      }
      .map(_.toTodoModel)
  }

  def createTodo(text: String): EitherT[Future, DaoError, TodoModel] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos => // insert to-do to db
        val objectId = BSONObjectID.generate()
        todos.insert(ordered = false)
          .one(
            TodoMongoModel(
              _id = objectId,
              text = text,
              isCompleted = false,
              isDeleted = false
            )
          )
          .map[BSONObjectID](_ => objectId)
      }
      .flatMap { objectId => // read created to-do from db
        EitherT.right[DaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoPayload(_id = Option(objectId)), Option.empty[JsObject])
              .cursor[TodoMongoModel]()
              .headOption
          }
      }
      .flatMap { todoMongoModel =>
        EitherT.fromOption[Future](todoMongoModel, NotFoundError)
          .leftWiden[DaoError]
      }
      .map(_.toTodoModel)
  }

  def updateTodo(id: String, payload: TodoPayload): EitherT[Future, DaoError, Unit] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[DaoError]
      .flatMap { objectId =>
        EitherT.right[DaoError](todos)
          .semiflatMap { todos =>
            // FIXME: Problem that it doesn't throw error if no elements match given selector!!!
            todos.update(ordered = false)
              .one(
                TodoPayload(_id = Option(objectId), isDeleted = Option(false)),
                Json.obj("$set" -> payload)
              )
          }
      }
      .flatMap { writeResult =>
        if (writeResult.ok) EitherT.rightT(())
        else EitherT.leftT(UnknownDaoError(writeResult.formattedErrorMessage))
      }
  }

  def updateTodos(payload: TodoPayload): EitherT[Future, DaoError, Unit] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos =>
        todos.update(ordered = false)
          .one(
            TodoPayload(isDeleted = Option(false)),
            Json.obj("$set" -> payload),
            multi = true
          )
      }
      .flatMap { writeResult =>
        if (writeResult.ok) EitherT.rightT(())
        else EitherT.leftT(UnknownDaoError(writeResult.formattedErrorMessage))
      }
  }

  def deleteTodo(id: String): EitherT[Future, DaoError, Unit] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[DaoError]
      .flatMap { objectId =>
        EitherT.right[DaoError](todos)
          .semiflatMap { todos =>
            todos.update(ordered = false)
              .one(
                TodoPayload(_id = Option(objectId), isDeleted = Option(false)),
                Json.obj("$set" -> TodoPayload(isDeleted = Some(true))),
                multi = true
              )
          }
      }
      .flatMap { writeResult =>
        if (writeResult.ok) EitherT.rightT(())
        else EitherT.leftT(UnknownDaoError(writeResult.formattedErrorMessage))
      }
  }

  def deleteTodos(filter: TodoPayload): EitherT[Future, DaoError, Unit] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos =>
        todos.update(ordered = false)
          .one(
            filter.copy(isDeleted = Some(false)),
            Json.obj("$set" -> TodoPayload(isDeleted = Option(true))),
            multi = true
          )
      }
      .flatMap { writeResult =>
        if (writeResult.ok) EitherT.rightT(())
        else EitherT.leftT(UnknownDaoError(writeResult.formattedErrorMessage))
      }
  }
}
