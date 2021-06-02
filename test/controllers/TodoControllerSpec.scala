package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.stream.Materializer
import cats.data.EitherT
import cats.implicits._
import controllers.todo.TodoController
import daos.todo.{InvalidIdError, TodoDao, TodoDaoError, TodoNotFoundError, UnknownDaoError}
import models.TodoModel
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsFalse, JsNumber, JsObject, JsString, JsTrue}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class TodoControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Results with MockitoSugar {

  implicit lazy val materializer: Materializer = app.materializer

  "GET /todos" should {
    "respond successfully with single todo" in {
      val todoDao = mock[TodoDao]
      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoDaoError](Seq(TodoTestData.firstTodoModel))
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsTrue,
        "content" -> JsArray(Seq(TodoTestData.firstTodoJson))
      ))
    }

    "respond successfully with multiple todos" in {
      val todoDao = mock[TodoDao]

      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoDaoError](
            Seq(
              TodoTestData.firstTodoModel,
              TodoTestData.secondTodoModel,
              TodoTestData.thirdTodoModel
            )
          )
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsTrue,
        "content" -> JsArray(Seq(
          TodoTestData.firstTodoJson,
          TodoTestData.secondTodoJson,
          TodoTestData.thirdTodoJson
        ))
      ))
    }

    "respond successfully with no todos" in {
      val todoDao = mock[TodoDao]
      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoDaoError](Seq.empty[TodoModel])
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsTrue,
        "content" -> JsArray(Seq.empty)
      ))
    }

    "respond with error" in {
      val todoDao = mock[TodoDao]
      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.leftT[Future, Seq[TodoModel]](UnknownDaoError("Test error"))
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsFalse,
        "errorCode" -> JsNumber(3),
        "errorMessage" -> JsString("Unknown database error. Test error")
      ))
    }
  }

  "GET /todos/:id:" should {
    "respond successfully if id correct" in {
      val id = "e93693e2996426a688920ace"

      val todoDao = mock[TodoDao]
      when(todoDao.getTodoById(id))
        .thenReturn(
          EitherT.rightT[Future, TodoDaoError](TodoTestData.firstTodoModel)
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, s"/todos/$id")
      val result = call(controller.getTodo(id), request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsTrue,
        "content" -> TodoTestData.firstTodoJson
      ))
    }

    "respond with errorCode=5 and errorMessage=\"Invalid id format. Incorrect id\" if id is incorrect" in {
      val id = "some-incorrect-id"

      val todoDao = mock[TodoDao]
      when(todoDao.getTodoById(id))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](InvalidIdError("Incorrect id"))
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, s"/todos/$id")
      val result = call(controller.getTodo(id), request)

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsFalse,
        "errorCode" -> JsNumber(5),
        "errorMessage" -> JsString("Invalid id format. Incorrect id")
      ))
    }

    "respond with errorCode=2 and errorMessage=\"Unable to find item\" if item wasn't found" in {
      val id = "da4edb8712a6c3f85f7b606f"

      val todoDao = mock[TodoDao]
      when(todoDao.getTodoById(id))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](TodoNotFoundError)
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, s"/todos/$id")
      val result = call(controller.getTodo(id), request)

      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsFalse,
        "errorCode" -> JsNumber(2),
        "errorMessage" -> JsString("Unable to find item")
      ))
    }
  }

  "POST /todos" should {
    "respond with created todo" in {
      val text = "Test Todo Text" // TODO: WE NEED TO HANDLE EMPTY TEXT! 
      val id = ""

      val todoDao = mock[TodoDao]
      when(todoDao.createTodo(text))
        .thenReturn(
          EitherT.rightT[Future, TodoDaoError](TodoModel(id = ""))
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, s"/todos/$id")
      val result = call(controller.getTodo(id), request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(Seq(
        "ok" -> JsTrue,
        "content" -> TodoTestData.firstTodoJson
      ))
    }
  }
}

private object TodoTestData {
  val (firstTodoModel, firstTodoJson) = (
    TodoModel(id = "e93693e2996426a688920ace", text = "test-text", isCompleted = true),
    JsObject(
      Seq(
        "id" -> JsString("e93693e2996426a688920ace"),
        "text" -> JsString("test-text"),
        "isCompleted" -> JsTrue
      )
    )
  )

  val (secondTodoModel, secondTodoJson) = (
    TodoModel(id = "test-id2", text = "test-text2", isCompleted = false),
    JsObject(
      Seq(
        "id" -> JsString("test-id2"),
        "text" -> JsString("test-text2"),
        "isCompleted" -> JsFalse
      )
    )
  )

  val (thirdTodoModel, thirdTodoJson) = (
    TodoModel(id = "test-id3", text = "test-text3", isCompleted = false),
    JsObject(
      Seq(
        "id" -> JsString("test-id3"),
        "text" -> JsString("test-text3"),
        "isCompleted" -> JsFalse
      )
    )
  )
}