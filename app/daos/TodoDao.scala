package daos

import models.TodoModel
import cats.implicits._
import cats.data.EitherT
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import collection._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TodoDao @Inject()(
  implicit executionContext: ExecutionContext,
  val reactiveMongoApi: ReactiveMongoApi
) {

  private def todos =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("todos"))

  def getAllTodos: Future[List[TodoModel]] = {
    val cursor = todos.map { todos =>
      todos.find(Json.obj(), Option.empty[JsObject])
        .cursor[TodoModel]()
    }

    cursor.flatMap {
      _.collect[List](-1, Cursor.FailOnError[List[TodoModel]]())
    }
  }

  private def parseId(id: String): Either[InvalidIdError, BSONObjectID] =
    BSONObjectID.parse(id)
      .map(Right(_))
      .recover { case throwable => Left(InvalidIdError(throwable)) }
      .get

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
}
