package services.todo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._
import cats.data.EitherT
import daos.todo.{TodoDao, TodoPayload}
import models.TodoModel

class TodoService @Inject()(
  implicit executionContext: ExecutionContext,
  val todoDao: TodoDao
) {

  def getAllTodos: EitherT[Future, TodoServiceError, Seq[TodoModel]] = {
    todoDao.getAllTodos
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  def getTodoById(id: String): EitherT[Future, TodoServiceError, TodoModel] = {
    todoDao.getTodoById(id)
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  def createTodo(text: String): EitherT[Future, TodoServiceError, TodoModel] = {
    val textEitherError = if (text.isEmpty) {
      EitherT.leftT[Future, String](UnableToCreateTodoWithEmptyTextError)
    } else {
      EitherT.rightT[Future, TodoServiceError](text)
    }

    textEitherError
      .flatMap(todoDao.createTodo(_))
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  // DO NOT PASS TODO PAYLOAD HERE!!
  def updateTodo(id: String, todoPayload: TodoPayload): EitherT[Future, TodoServiceError, Unit] = {
    todoDao.updateTodo(id, todoPayload)
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  def updateTodos(todoPayload: TodoPayload): EitherT[Future, TodoServiceError, Unit] = {
    todoDao.updateTodos()
  }
}