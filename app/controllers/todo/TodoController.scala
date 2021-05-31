package controllers.todo

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.implicits._
import controllers.ControllerError
import daos.todo.TodoDao
import models.TodoModel
import play.api.mvc._
import utils.ContentToResultMappingUtil._
import utils.{ContentToResultMappingUtil, ControllerErrorToResultMapper, DaoErrorToControllerErrorMapper, JsonParsingUtil}

class TodoController @Inject()(
  components: ControllerComponents,
  val todoDao: TodoDao
) extends AbstractController(components) {

  def getAllTodos: Action[AnyContent] = Action.async {
    throw new IllegalStateException("TEST THROWN!!!")
    todoDao.getAllTodos
      .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Seq[TodoModel]]
      )
  }

  def getTodo(id: String): Action[AnyContent] = Action.async {
    todoDao.getTodoById(id)
      .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[TodoModel]
      )
  }

  def createTodo(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[CreateTodoRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.createTodo(requestBody.text)
          .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[TodoModel]
      )
  }

  def updateTodo(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodoRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.updateTodo(id, requestBody.toTodoPayload)
          .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def updateTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodosRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.updateTodos(requestBody.toTodoPayload)
          .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async {
    todoDao.deleteTodo(id)
      .leftMap(DaoErrorToControllerErrorMapper)
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def deleteTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[DeleteTodosRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.deleteTodos(requestBody.toTodoSelector)
          .leftMap[ControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        ControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit] // FIXME: maybe there is some more beautiful way like ContentToResultMapper[T]
                                             //        but I don't know how to implement clean and nice T-parametrized object
      )
  }
}
