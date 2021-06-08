package daos.todo

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._
import models.TodoModel
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, WriteConcern}
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.compat._
import reactivemongo.play.json.collection._
import utils.BSONObjectIDUtil
import utils.ReactiveMongoErrorsUtil._

class TodoDaoImpl @Inject()(
  implicit executionContext: ExecutionContext,
  val reactiveMongoApi: ReactiveMongoApi
) extends TodoDao {

  private def todos =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("todos"))

  def getAllTodos: EitherT[Future, TodoDaoError, Seq[TodoModel]] = {
    EitherT.right[TodoDaoError](todos)
      .semiflatMap { todos =>
        todos.find(TodoSelector(isDeleted = Option(false)), Option.empty[JsObject])
          .cursor[TodoMongoModel]()
          .collect[List](-1, Cursor.FailOnError[List[TodoMongoModel]]())
      }
      .map { todoMongoModels =>
        todoMongoModels.map(_.toTodoModel)
      }
  }

  def getTodoById(id: String): EitherT[Future, TodoDaoError, TodoModel] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[TodoDaoError]
      .flatMap { objectId =>
        EitherT.right[TodoDaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoSelector(_id = Option(objectId), isDeleted = Option(false)), Option.empty[JsObject])
              .cursor[TodoMongoModel]()
              .headOption
          }
      }
      .flatMap { todoMongoModel =>
        EitherT.fromOption[Future](todoMongoModel, CannotFindTodoWithSuchId(id))
          .leftWiden[TodoDaoError]
      }
      .map(_.toTodoModel)
  }

  def createTodo(text: String): EitherT[Future, TodoDaoError, TodoModel] = {
    EitherT.right[TodoDaoError](todos)
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
        EitherT.right[TodoDaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoSelector(_id = Option(objectId)), Option.empty[JsObject])
              .one[TodoMongoModel]
          }
      }
      .flatMap { todoMongoModel =>
        EitherT.fromOption[Future](todoMongoModel, UnknownDaoError("Unable to get newly created todo from database"))
          .leftWiden[TodoDaoError]
      }
      .map(_.toTodoModel)
  }

  def updateTodo(id: String, payload: TodoPayload): EitherT[Future, TodoDaoError, TodoModel] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[TodoDaoError]
      .flatMap { objectId =>
        EitherT.right[TodoDaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoSelector(_id = Option(objectId)), Option.empty[JsObject])
              .one[TodoMongoModel]
          }
      }
      .flatMap { todo =>
        EitherT.fromOption[Future](todo, CannotFindTodoWithSuchId(id))
          .leftWiden[TodoDaoError]
      }
      .flatMap { todo =>
        if (!todo.isDeleted) EitherT.rightT[Future, TodoDaoError](todo)
        else EitherT.leftT[Future, TodoMongoModel](CannotUpdateDeletedTodoError)
          .leftWiden[TodoDaoError]
      }
      .flatMap { todo =>
        EitherT.right[TodoDaoError](todos)
          .semiflatMap { todos =>
            todos.findAndUpdate(
              TodoSelector(_id = Option(todo._id)),
              Json.obj("$set" -> payload),
              fetchNewObject = true,
              upsert = false,
              sort = None,
              fields = None,
              bypassDocumentValidation = false,
              writeConcern = WriteConcern.Default,
              maxTime = None,
              collation = None,
              arrayFilters = Seq.empty
            )
          }
      }
      .flatMap[TodoDaoError, TodoMongoModel] { updateResult =>
        EitherT.fromOption[Future](
          updateResult.result[TodoMongoModel],
          UnknownDaoError(
            updateResult.lastError.flatMap(_.err)
              .getOrElse("Unknown error while updating existing todo")
          )
        )
      }
      .map(_.toTodoModel)
  }

  def updateTodos(payload: TodoPayload): EitherT[Future, TodoDaoError, Unit] = {
    EitherT.right[TodoDaoError](todos)
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

  def deleteTodo(id: String): EitherT[Future, TodoDaoError, Unit] = {
    BSONObjectIDUtil.parseEither(id)
      .toEitherT[Future]
      .leftWiden[TodoDaoError]
      .flatMap { objectId =>
        EitherT.right[TodoDaoError](todos)
          .semiflatMap { todos =>
            todos.find(TodoSelector(_id = Option(objectId)), Option.empty)
              .one[TodoMongoModel]
          }
      }
      .flatMap { todoMongoModel =>
        EitherT.fromOption[Future](todoMongoModel, CannotFindTodoWithSuchId(id))
          .leftWiden[TodoDaoError]
      }
      .flatMap { todoMongoModel =>
        if (!todoMongoModel.isDeleted) {
          EitherT.right[TodoDaoError](todos)
            .semiflatMap { todos =>
              todos.update(ordered = false)
                .one(
                  TodoSelector(_id = Option(todoMongoModel._id), isDeleted = Option(false)),
                  Json.obj("$set" -> TodoPayload(isDeleted = Some(true))),
                  multi = true
                )
            }
        } else {
          EitherT.leftT[Future, UpdateWriteResult](CannotDeleteAlreadyDeletedTodoError)
            .leftWiden[TodoDaoError]
        }
      }
      .flatMap { writeResult =>
        if (writeResult.ok) EitherT.rightT(())
        else EitherT.leftT(UnknownDaoError(writeResult.formattedErrorMessage))
      }
  }

  def deleteTodos(selector: TodoSelector): EitherT[Future, TodoDaoError, Unit] = {
    EitherT.right[TodoDaoError](todos)
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
