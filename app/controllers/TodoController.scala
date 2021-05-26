package controllers

import daos.TodoDao
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import play.mvc.Http.RequestBody

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

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
    todoDao.getTodoById(id).value
      .collect {
        case Right(todoModel) => Ok(Json.toJson(todoModel))
        case Left(error) => InternalServerError("error")
      }
  }

  /*def createTodo: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    throw new NotImplementedError()
  }

  def updateTodo: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    throw new NotImplementedError()
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async {
    throw new NotImplementedError()
  }*/
}
