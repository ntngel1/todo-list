package controllers.todo

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits._
import controllers.{ControllerError, JsonBodyParsingError, NoRequestBodyError, PersistenceLayerError}
import daos.TodoDao
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._

class TodoController @Inject()(
  components: ControllerComponents,
  val todoDao: TodoDao
) extends AbstractController(components) {

  def getAllTodos: Action[AnyContent] = Action.async {
    todoDao.getAllTodos
      .map { todos =>
        Ok(Json.toJson(todos))
      }
  }

  def getTodo(id: String): Action[AnyContent] = Action.async {
    todoDao.getTodoById(id)
      .fold(
        _ => InternalServerError("error"), // TODO: return corresponding error.
        //       Maybe create some Response object with encapsulated error?
        todoModel => Ok(Json.toJson(todoModel))
      )
  }

  def createTodo: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    EitherT.fromOption[Future](request.body.asJson, NoRequestBodyError)
      .leftWiden[ControllerError]
      .map(Json.fromJson[CreateTodoRequestBody](_))
      .flatMap { jsonParsingResult =>
        jsonParsingResult match {
          case JsSuccess(value, _) => EitherT.rightT[Future, ControllerError](value)
          case JsError(errors) => EitherT.leftT[Future, CreateTodoRequestBody](JsonBodyParsingError(errors.mkString(", ")))
                                         .leftWiden[ControllerError]
        }
      }
      .flatMap { requestBody =>
        todoDao.createTodo(requestBody.text)
          .leftMap(_ => PersistenceLayerError) // TODO: return corresponding error
          .leftWiden[ControllerError]
      }
      .fold(
        _ => InternalServerError("create todo error"), // TODO: return corresponding error
        todoModel => Ok(Json.toJson(todoModel))
      )
  }

  /*def updateTodo: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    throw new NotImplementedError()
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async {
    throw new NotImplementedError()
  }*/
}
