package controllers.todo

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits._
import controllers.common.ContentToResultMapper._
import controllers.common.{ControllerError, ControllerErrorToResultMapper, NoRequestBodyError}
import controllers.todo.requestbody.{CreateTodoRequestBody, DeleteTodosRequestBody, UpdateTodoRequestBody, UpdateTodosRequestBody}
import models.TodoModel
import play.api.mvc._
import services.todo.TodoService
import utils.JsonParsingUtil

class TodoController @Inject()(
  components: ControllerComponents,
  val todoService: TodoService
) extends AbstractController(components) {

  def getAllTodos: Action[AnyContent] = Action.async {
    todoService.getAllTodos
      .leftMap(TodoServiceErrorToControllerErrorMapper)
      .fold(ControllerErrorToResultMapper, mapContentToResult[Seq[TodoModel]])
  }

  def getTodo(id: String): Action[AnyContent] = Action.async {
    todoService.getTodoById(id)
      .leftMap(TodoServiceErrorToControllerErrorMapper)
      .fold(ControllerErrorToResultMapper, mapContentToResult[TodoModel])
  }

  def createTodo(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[CreateTodoRequestBody](request.body.asJson)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoService.createTodo(requestBody.text)
          .leftMap(TodoServiceErrorToControllerErrorMapper)
      }
      .fold(ControllerErrorToResultMapper, mapContentToResult[TodoModel])
  }

  def updateTodo(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodoRequestBody](request.body.asJson)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoService.updateTodo(id, text = requestBody.text, isCompleted = requestBody.isCompleted)
          .leftMap(TodoServiceErrorToControllerErrorMapper)
      }
      .fold(ControllerErrorToResultMapper, mapContentToResult[Unit])
  }

  def updateTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[UpdateTodosRequestBody](request.body.asJson)
      .toEitherT[Future]
      .flatMap { requestBody =>
        todoService.updateTodos(requestBody.isCompleted)
          .leftMap(TodoServiceErrorToControllerErrorMapper)
      }
      .fold(ControllerErrorToResultMapper, mapContentToResult[Unit])
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async {
    todoService.deleteTodo(id)
      .leftMap(TodoServiceErrorToControllerErrorMapper)
      .fold(ControllerErrorToResultMapper, mapContentToResult[Unit])
  }

  def deleteTodos(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    JsonParsingUtil.parse[DeleteTodosRequestBody](request.body.asJson)
      .toEitherT[Future]
      .biflatMap({
        case NoRequestBodyError => todoService.deleteTodos()
          .leftMap(TodoServiceErrorToControllerErrorMapper)
        case error => EitherT.leftT[Future, Unit](error)
      }, { requestBody =>
        todoService.deleteTodos(filterByIsCompleted = Option(requestBody.isCompleted))
          .leftMap(TodoServiceErrorToControllerErrorMapper)
      })
      .fold(ControllerErrorToResultMapper, mapContentToResult[Unit])
  }
}
