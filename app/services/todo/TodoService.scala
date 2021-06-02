package services.todo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._
import cats.data.EitherT
import daos.todo.{TodoDao, TodoPayload, TodoSelector}
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
      .flatMap { text =>
        todoDao.createTodo(text)
          .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
      }
  }

  def updateTodo(id: String, text: Option[String], isCompleted: Option[Boolean]): EitherT[Future, TodoServiceError, Unit] = {
    if (text.isDefined || isCompleted.isDefined) {
      todoDao.updateTodo(id, TodoPayload(text = text, isCompleted = isCompleted))
        .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
    } else {
      EitherT.leftT[Future, Unit](NoFieldsPassedToUpdateTodoError)
    }
  }

  def updateTodos(isCompleted: Boolean): EitherT[Future, TodoServiceError, Unit] = {
    todoDao.updateTodos(TodoPayload(isCompleted = Option(isCompleted)))
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  def deleteTodo(id: String): EitherT[Future, TodoServiceError, Unit] = {
    todoDao.deleteTodo(id)
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }

  def deleteTodos(filterByIsCompleted: Option[Boolean] = Option.empty): EitherT[Future, TodoServiceError, Unit] = {
    todoDao.deleteTodos(TodoSelector(isCompleted = filterByIsCompleted))
      .leftMap(TodoDaoErrorToTodoServiceErrorMapper)
  }
}