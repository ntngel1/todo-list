package daos

import models.TodoModel
import cats.data.EitherT
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import collection._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toBifunctorOps

class TodoDao @Inject()(
  implicit executionContext: ExecutionContext,
  val reactiveMongoApi: ReactiveMongoApi
) {

  private def todos =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("todos"))

  private def parseId(id: String): Either[InvalidIdError, BSONObjectID] =
    BSONObjectID.parse(id)
      .map(Right(_))
      .recover { case throwable => Left(InvalidIdError(throwable)) }
      .get

  def getAllTodos: Future[List[TodoModel]] = {
    val cursor = todos.map { todos =>
      todos.find(Json.obj(), Option.empty[JsObject])
        .cursor[TodoModel]()
    }

    cursor.flatMap {
      _.collect[List](-1, Cursor.FailOnError[List[TodoModel]]())
    }
  }

  def getTodoById(id: String): EitherT[Future, DaoError, TodoModel] = {
    EitherT.fromEither[Future](parseId(id))
      .leftWiden[DaoError]
      .flatMap { objectId =>
        EitherT.right[DaoError](todos)
          .map { todos =>
            todos.find(Json.obj("_id" -> objectId), Option.empty[JsObject])
              .cursor[TodoModel]()
          }
      }
      .semiflatMap {
        _.collect[List](1, Cursor.FailOnError[List[TodoModel]]())
      }
      .flatMap { todosList =>
        EitherT.fromOption(todosList.headOption, NotFoundError)
      }
  }

  def createTodo(text: String): EitherT[Future, DaoError, TodoModel] = {
    EitherT.right[DaoError](todos)
      .semiflatMap { todos => // insert to-do to db
        val objectId = BSONObjectID.generate()
        todos.insert(ordered = false)
          .one(
            TodoModel(
              _id = objectId,
              text = text,
              isCompleted = false,
              isDeleted = false
            )
          )
          .map[BSONObjectID](_ => objectId)
      }
      .semiflatMap { objectId => // read created to-do from db
        todos.map(
          _.find(Json.obj("_id" -> objectId), Option.empty[JsObject])
            .cursor[TodoModel]()
        )
      }
      .semiflatMap(_.headOption)
      .flatMap { todoOption =>
        EitherT.fromOption[Future](todoOption, NotFoundError)
      }
  }
}
