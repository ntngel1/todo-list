package daos.todo

import scala.concurrent.Future
import cats.data.EitherT
import com.google.inject.ImplementedBy
import models.TodoModel

@ImplementedBy(classOf[TodoDaoImpl])
trait TodoDao {
  def getAllTodos: EitherT[Future, TodoDaoError, Seq[TodoModel]]

  def getTodoById(id: String): EitherT[Future, TodoDaoError, TodoModel]

  def createTodo(text: String): EitherT[Future, TodoDaoError, TodoModel]

  def updateTodo(id: String, payload: TodoPayload): EitherT[Future, TodoDaoError, Unit]

  def updateTodos(payload: TodoPayload): EitherT[Future, TodoDaoError, Unit]

  def deleteTodo(id: String): EitherT[Future, TodoDaoError, Unit]

  def deleteTodos(selector: TodoSelector): EitherT[Future, TodoDaoError, Unit]
}
