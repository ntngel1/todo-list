package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.stream.Materializer
import cats.data.EitherT
import cats.implicits._
import controllers.todo.TodoController
import daos.DaoError
import daos.todo.TodoDao
import models.TodoModel
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsFalse, JsObject, JsString, JsTrue}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class TodoControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Results with MockitoSugar {

  implicit lazy val materializer: Materializer = app.materializer

  "getAllTodos request" should {
    "respond successfully with only todo" in {
      val todoDao = mock[TodoDao]
      val todoModel = TodoModel(id = "test-id", text = "test-text", isCompleted = true)
      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, DaoError](Seq(todoModel))
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(
        Seq(
          "ok" -> JsTrue,
          "content" -> JsArray(
            Seq(
              JsObject(
                Seq(
                  "id" -> JsString("test-id"),
                  "text" -> JsString("test-text"),
                  "isCompleted" -> JsTrue
                )
              )
            )
          )
        )
      )
    }

    "respond successfully with multiple todos" in {
      val todoDao = mock[TodoDao]
      val todoModels = Seq(
        TodoModel(id = "test-id", text = "test-text", isCompleted = true),
        TodoModel(id = "test-id2", text = "test-text2", isCompleted = false),
        TodoModel(id = "test-id3", text = "test-text3", isCompleted = false)
      )

      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, DaoError](todoModels)
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(
        Seq(
          "ok" -> JsTrue,
          "content" -> JsArray(
            Seq(
              JsObject(
                Seq(
                  "id" -> JsString("test-id"),
                  "text" -> JsString("test-text"),
                  "isCompleted" -> JsTrue
                )
              ),
              JsObject(
                Seq(
                  "id" -> JsString("test-id2"),
                  "text" -> JsString("test-text2"),
                  "isCompleted" -> JsFalse
                )
              ),
              JsObject(
                Seq(
                  "id" -> JsString("test-id3"),
                  "text" -> JsString("test-text3"),
                  "isCompleted" -> JsFalse
                )
              )
            )
          )
        )
      )
    }

    "respond successfully with no todos" in {
      val todoDao = mock[TodoDao]
      when(todoDao.getAllTodos)
        .thenReturn(
          EitherT.rightT[Future, DaoError](Seq.empty[TodoModel])
        )

      val controller = new TodoController(stubControllerComponents(), todoDao)
      val request = FakeRequest(GET, "/todos")
      val result = call(controller.getAllTodos, request)

      status(result) mustBe OK
      contentAsJson(result) mustBe JsObject(
        Seq(
          "ok" -> JsTrue,
          "content" -> JsArray(Seq.empty)
        )
      )
    }
  }
}