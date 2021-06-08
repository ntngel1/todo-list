package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.stream.Materializer
import cats.data.EitherT
import cats.implicits._
import controllers.todo.TodoController
import daos.todo.UnknownDaoError
import models.TodoModel
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsBoolean, JsFalse, JsNull, JsNumber, JsObject, JsString, JsTrue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.todo._

class TodoControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Results with MockitoSugar {

  implicit lazy val materializer: Materializer = app.materializer

  private val todoService = mock[TodoService]
  private val todoController = new TodoController(stubControllerComponents(), todoService)

  "GET /todos" must {
    "respond successfully with single todo" in {
      // ARRANGE
      when(todoService.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](Seq(TodoTestData.firstTodoModel))
        )

      // ACT
      val request = FakeRequest(GET, "/todos")
      val result = todoController.getAllTodos.apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "ok" -> JsTrue,
        "content" -> Json.arr(TodoTestData.firstTodoJson)
      )
    }

    "respond successfully with multiple todos" in {
      // ARRANGE
      when(todoService.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](
            Seq(
              TodoTestData.firstTodoModel,
              TodoTestData.secondTodoModel,
              TodoTestData.thirdTodoModel
            )
          )
        )

      // ACT
      val request = FakeRequest(GET, "/todos")
      val result = todoController.getAllTodos.apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "ok" -> JsTrue,
        "content" -> Json.arr(
          TodoTestData.firstTodoJson,
          TodoTestData.secondTodoJson,
          TodoTestData.thirdTodoJson
        )
      )
    }

    "respond successfully with no todos" in {
      // ARRANGE
      when(todoService.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](Seq.empty[TodoModel])
        )

      // ACT
      val request = FakeRequest(GET, "/todos")
      val result = todoController.getAllTodos.apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "ok" -> JsTrue,
        "content" -> JsArray.empty
      )
    }

    "respond with errorCode=102 on unknown DAO error" in {
      // ARRANGE
      when(todoService.getAllTodos)
        .thenReturn(
          EitherT.leftT[Future, Seq[TodoModel]](DaoLayerError(UnknownDaoError("Test error")))
        )

      // ACT
      val request = FakeRequest(GET, "/todos")
      val result = todoController.getAllTodos.apply(request)

      // ASSERT
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(102))
    }
  }

  "GET /todos/:id:" must {
    "respond successfully if id is correct and todo was found" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"

      when(todoService.getTodoById(id))
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](TodoTestData.firstTodoModel)
        )

      // ACT
      val request = FakeRequest(GET, s"/todos/$id")
      val result = todoController.getTodo(id).apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "ok" -> JsTrue,
        "content" -> TodoTestData.firstTodoJson
      )
    }

    "respond with errorCode=104 if id is incorrect" in {
      // ARRANGE
      val id = "some-incorrect-id"

      when(todoService.getTodoById(id))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](InvalidTodoIdError(""))
        )

      // ACT
      val request = FakeRequest(GET, s"/todos/$id")
      val result = todoController.getTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(104))
    }

    "respond with errorCode=101 if item wasn't found" in {
      // ARRANGE
      val id = "da4edb8712a6c3f85f7b606f"

      when(todoService.getTodoById(id))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](TodoWithSuchIdNotFoundError(id))
        )

      // ACT
      val request = FakeRequest(GET, s"/todos/$id")
      val result = todoController.getTodo(id).apply(request)

      // ASSERT
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(101))
    }
  }

  "POST /todos" must {
    "respond successfully with created todo if passed data is correct" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"
      val text = "Test Todo Text"
      val todoModel = TodoModel(id = id, text = text, isCompleted = false)

      when(todoService.createTodo(text))
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](todoModel)
        )

      // ACT
      val request = FakeRequest(POST, "/todos")
        .withJsonBody(Json.obj("text" -> JsString(text)))
      val result = todoController.createTodo().apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "ok" -> JsTrue,
        "content" -> Json.obj(
          "id" -> JsString(id),
          "text" -> JsString(text),
          "isCompleted" -> JsFalse
        )
      )
    }

    "respond with errorCode=1 if no request body passed" in {
      // ACT
      val request = FakeRequest(POST, "/todos")
      val result = todoController.createTodo().apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(1))
    }

    "respond with errorCode=100 if passed text is blank" in {
      // ARRANGE
      val text = "Test Todo Text"

      when(todoService.createTodo(text))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](UnableToCreateTodoWithEmptyTextError)
        )

      // ACT
      val request = FakeRequest(POST, "/todos")
        .withJsonBody(Json.obj("text" -> JsString(text)))
      val result = todoController.createTodo().apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(100))
    }
  }

  "PATCH /todos/:id" must {
    "respond successfully if passed update data is correct" in {
      // ARRANGE

      when(
        todoService.updateTodo(
          TodoTestData.firstTodoModel.id,
          text = Option(TodoTestData.firstTodoModel.text),
          isCompleted = Option(TodoTestData.firstTodoModel.isCompleted)
        )
      )
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](TodoTestData.firstTodoModel)
        )

      // ACT
      val request = FakeRequest(PATCH, s"/todos/${TodoTestData.firstTodoModel.id}")
        .withJsonBody(
          Json.obj(
            "text" -> TodoTestData.firstTodoModel.text,
            "isCompleted" -> TodoTestData.firstTodoModel.isCompleted
          )
        )
      val result = todoController.updateTodo(TodoTestData.firstTodoModel.id).apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("ok" -> JsTrue, "content" -> TodoTestData.firstTodoJson)
    }

    "respond with errorCode=104 if id is incorrect" in {
      // ARRANGE
      val id = "some-incorrect-id"
      val text = "New Text"

      when(todoService.updateTodo(id, text = Option(text), isCompleted = Option.empty))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](InvalidTodoIdError(""))
        )

      // ACT
      val request = FakeRequest(PATCH, s"/todos/$id")
        .withJsonBody(Json.obj("text" -> text))
      val result = todoController.updateTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(104))
    }

    "respond with errorCode=1 if no request body passed" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"

      // ACT
      val request = FakeRequest(PATCH, s"/todos/$id")
      val result = todoController.updateTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(1))
    }

    "respond with errorCode=105 if no fields passed to update todo" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"
      when(todoService.updateTodo(id, text = Option.empty, isCompleted = Option.empty))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](NoFieldsPassedToUpdateTodoError)
        )

      // ACT
      val request = FakeRequest(PATCH, s"/todos/$id")
        .withJsonBody(Json.obj())
      val result = todoController.updateTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(105))
    }

    "respond with errorCode=107 if passed new text is empty" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"
      when(todoService.updateTodo(id, text = Option(""), isCompleted = Option.empty))
        .thenReturn(
          EitherT.leftT[Future, TodoModel](UnableToMakeTodoTextEmptyError)
        )

      // ACT
      val request = FakeRequest(PATCH, s"/todos/$id")
        .withJsonBody(Json.obj("text" -> ""))
      val result = todoController.updateTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(107))
    }
  }

  "PATCH /todos" must {
    "respond successfully if passed update data is correct" in {
      // ARRANGE
      val isCompleted = true

      when(todoService.updateTodos(isCompleted))
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](())
        )

      // ACT
      val request = FakeRequest(PATCH, "/todos")
        .withJsonBody(Json.obj("isCompleted" -> JsBoolean(isCompleted)))
      val result = todoController.updateTodos().apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("ok" -> JsTrue, "content" -> JsNull)
    }

    "respond with errorCode=1 if no request body passed" in {
      // ACT
      val request = FakeRequest(PATCH, "/todos")
      val result = todoController.updateTodos().apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(1))
    }

    "respond with errorCode=4 if invalid json passed" in {
      // ACT
      val request = FakeRequest(PATCH, "/todos")
        .withJsonBody(Json.obj())
      val result = todoController.updateTodos().apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(4))
    }
  }

  "DELETE /todos/:id" must {
    "respond successfully if id is correct" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"

      when(todoService.deleteTodo(id))
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](())
        )

      // ACT
      val request = FakeRequest(DELETE, s"/todos/$id")
      val result = todoController.deleteTodo(id).apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("ok" -> JsTrue, "content" -> JsNull)
    }

    "respond with errorCode=104 if id is incorrect" in {
      // ARRANGE
      val id = "some-incorrect-id"

      when(todoService.deleteTodo(id))
        .thenReturn(
          EitherT.leftT[Future, Unit](InvalidTodoIdError(""))
        )

      // ACT
      val request = FakeRequest(DELETE, s"/todos/$id")
      val result = todoController.deleteTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(104))
    }

    "respond with errorCode=101 if todo with such id doesn't exist" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"

      when(todoService.deleteTodo(id))
        .thenReturn(
          EitherT.leftT[Future, Unit](TodoWithSuchIdNotFoundError(id))
        )

      // ACT
      val request = FakeRequest(DELETE, s"/todos/$id")
      val result = todoController.deleteTodo(id).apply(request)

      // ASSERT
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(101))
    }

    "respond with errorCode=106 if todo with such id already deleted" in {
      // ARRANGE
      val id = "e93693e2996426a688920ace"

      when(todoService.deleteTodo(id))
        .thenReturn(
          EitherT.leftT[Future, Unit](UnableToDeleteAlreadyDeletedTodoError)
        )

      // ACT
      val request = FakeRequest(DELETE, s"/todos/$id")
      val result = todoController.deleteTodo(id).apply(request)

      // ASSERT
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe a[JsObject]

      val json = contentAsJson(result).asInstanceOf[JsObject]
      json.fields must contain("ok", JsFalse)
      json.fields must contain("errorCode", JsNumber(106))
    }
  }

  "DELETE /todos" must {
    "respond successfully if passed data is correct" in {
      // ARRANGE
      val filterByIsCompleted = true
      when(todoService.deleteTodos(filterByIsCompleted = Option(filterByIsCompleted)))
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](())
        )

      // ACT
      val request = FakeRequest(DELETE, "/todos")
        .withJsonBody(Json.obj("isCompleted" -> filterByIsCompleted))
      val result = todoController.deleteTodos().apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("ok" -> JsTrue, "content" -> JsNull)
    }

    "respond successfully if no request body passed" in {
      // ARRANGE
      when(todoService.deleteTodos())
        .thenReturn(
          EitherT.rightT[Future, TodoServiceError](())
        )

      // ACT
      val request = FakeRequest(DELETE, "/todos")
      val result = todoController.deleteTodos().apply(request)

      // ASSERT
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("ok" -> JsTrue, "content" -> JsNull)
    }
  }
}

private object TodoTestData {
  val (firstTodoModel, firstTodoJson) = (
    TodoModel(id = "e93693e2996426a688920ace", text = "test-text", isCompleted = true),
    Json.obj(
      "id" -> JsString("e93693e2996426a688920ace"),
      "text" -> JsString("test-text"),
      "isCompleted" -> JsTrue
    )
  )

  val (secondTodoModel, secondTodoJson) = (
    TodoModel(id = "test-id2", text = "test-text2", isCompleted = false),
    Json.obj(
      "id" -> JsString("test-id2"),
      "text" -> JsString("test-text2"),
      "isCompleted" -> JsFalse
    )
  )

  val (thirdTodoModel, thirdTodoJson) = (
    TodoModel(id = "test-id3", text = "test-text3", isCompleted = false),
    Json.obj(
      "id" -> JsString("test-id3"),
      "text" -> JsString("test-text3"),
      "isCompleted" -> JsFalse
    )
  )
}