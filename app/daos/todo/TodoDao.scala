package daos.todo

import scala.concurrent.Future

import cats.data.EitherT
import com.google.inject.ImplementedBy
import daos.DaoError
import models.TodoModel

@ImplementedBy(classOf[TodoDaoImpl])
trait TodoDao {
  def getAllTodos: EitherT[Future, DaoError, Seq[TodoModel]]
  def getTodoById(id: String): EitherT[Future, DaoError, TodoModel]

  def createTodo(text: String): EitherT[Future, DaoError, TodoModel]

  def updateTodo(id: String, payload: TodoPayload): EitherT[Future, DaoError, Unit]
  def updateTodos(payload: TodoPayload): EitherT[Future, DaoError, Unit]

  def deleteTodo(id: String): EitherT[Future, DaoError, Unit]
  def deleteTodos(selector: TodoSelector): EitherT[Future, DaoError, Unit]
}
