package controllers.todo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.implicits._
import controllers.TodoControllerError
import controllers.todo.requestbody.{CreateTodoRequestBody, DeleteTodosRequestBody, UpdateTodoRequestBody, UpdateTodosRequestBody}
import daos.todo.TodoDao
import models.TodoModel
import play.api.mvc._
import services.todo.TodoService
import utils.ContentToResultMappingUtil._
import utils.{ContentToResultMappingUtil, JsonParsingUtil}

class TodoController @Inject()(
  components: ControllerComponents,
  val todoService: TodoService,
  val todoDao: TodoDao
) extends AbstractController(components) {

  def getAllTodos: Action[AnyContent] = Action.async {
    todoDao.getAllTodos
      .leftMap[TodoControllerError](DaoErrorToControllerErrorMapper)
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Seq[TodoModel]]
      )
  }

  def getTodo(id: String): Action[AnyContent] = Action.async {
    todoDao.getTodoById(id)
      .leftMap[TodoControllerError](DaoErrorToControllerErrorMapper)
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[TodoModel]
      )
  }

  def createTodo(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[CreateTodoRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoService.createTodo(requestBody.text)
          .leftMap(TodoServiceErrorToTodoControllerErrorMapper)
      }
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[TodoModel]
      )
  }

  def updateTodo(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodoRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.updateTodo(id, requestBody.toTodoPayload)
          .leftMap[TodoControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def updateTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodosRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.updateTodos(requestBody.toTodoPayload)
          .leftMap[TodoControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async {
    todoDao.deleteTodo(id)
      .leftMap(DaoErrorToControllerErrorMapper)
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit]
      )
  }

  def deleteTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[DeleteTodosRequestBody](request)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoDao.deleteTodos(requestBody.toTodoSelector)
          .leftMap[TodoControllerError](DaoErrorToControllerErrorMapper)
      }
      .fold(
        TodoControllerErrorToResultMapper,
        ContentToResultMappingUtil.map[Unit] // FIXME: maybe there is some more beautiful way to map from some content T
                                             //  to Play Frameowork's Result like ContentToResultMapper[T] but I don't
                                             //  know how to implement clean and nice T-parametrized object
      )
  }
}
