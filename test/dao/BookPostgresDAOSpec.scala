package dao

import commons.{PostgresDataHandlerSpec, RandomDataGenerator}
import models.dao.{DatabaseExecutionContext, UserDAO}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger


class BookPostgresDAOSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with PostgresDataHandlerSpec {

  implicit val ec: DatabaseExecutionContext = app.injector.instanceOf(classOf[DatabaseExecutionContext])


  lazy val userService = new UserDAO(database)(ec)

  "Book service" should {
    "create a new book" in {

     }

    "update a book" in {

    }
    "delete a book" in {

    }
  }
}
