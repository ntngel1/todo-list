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

class TodoDaoImpl @Inject()(
  implicit executionContext: ExecutionContext,
  val reactiveMongoApi: ReactiveMongoApi
) extends TodoDao {

  private def todos =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("todos"))

  def getAllTodos: EitherT[Future, DaoError, Seq[TodoModel]] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos =>
        todos.find(TodoSelector(isDeleted = Option(false)), Option.empty[JsObject])
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
            todos.find(TodoSelector(_id = Option(objectId), isDeleted = Option(false)), Option.empty[JsObject])
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
            todos.find(TodoSelector(_id = Option(objectId)), Option.empty[JsObject])
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
            val selector = TodoSelector(_id = Option(objectId), isDeleted = Option(false))
            // FIXME: no item may be updated and no error thrown. How can we throw an error if no elements found by
            //        given selector?
            todos.update(ordered = false)
              .one(selector, Json.obj("$set" -> payload))
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
            TodoSelector(isDeleted = Option(false)),
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
                TodoSelector(_id = Option(objectId), isDeleted = Option(false)),
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

  def deleteTodos(selector: TodoSelector): EitherT[Future, DaoError, Unit] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos =>
        todos.update(ordered = false)
          .one(
            selector.copy(isDeleted = Some(false)),
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
