package daos

import cats.data.EitherT
import cats.implicits._
import models.TodoModel
import models.TodoModel.bsonHandler
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TodoDao @Inject()(
                         implicit executionContext: ExecutionContext,
                         val reactiveMongoApi: ReactiveMongoApi
                       ) {

  private def todos =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("todos"))

  def getAllTodos: Future[List[TodoModel]] = {
    val cursor = todos.map { todos =>
      implicit val bsonHandler = Macros.handler[TodoModel] // TODO Remove
      todos.find(BSONDocument(), Option.empty[TodoModel])
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
    implicit val bsonHandler = Macros.handler[TodoModel] // TODO Remove
    EitherT.fromEither[Future](parseId(id))
      .leftWiden[DaoError]
      .flatMap { objectId =>
        EitherT.right[DaoError](todos)
          .map { todos =>
            todos.find(BSONDocument("_id" -> objectId))
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
