package dao

import commons.{PostgresDataHandlerSpec, RandomDataGenerator}
import models.dao.{DatabaseExecutionContext, UserDAO}
import models.User
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger


class UserPostgresDAOSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with PostgresDataHandlerSpec  {

  implicit val ec: DatabaseExecutionContext = app.injector.instanceOf(classOf[DatabaseExecutionContext])


  lazy val userDAO = new UserDAO(database)(ec)

  "User service" should {

    "create a new user" in {

      val name = "Yun"
      val email = RandomDataGenerator.email.string
      val pw = RandomDataGenerator.hiddenPassword.string

      val user = User(None, name, email, pw)
      val result = userDAO.insert(user)

      whenReady(result) { maybeUser =>

        maybeUser.map { user =>
          user.name mustBe name
          user.email mustBe email
          //user.pw mustBe empty
        }

      }
    }

    "create a new user with already existing email" in {

      val name = "Yun"
      val email = "admin@admin.com"
      val pw = RandomDataGenerator.hiddenPassword.string

      val user = User(None, name, email, pw)

      val result = userDAO.insert(user)

      whenReady(result) { maybeUser =>
        maybeUser.map { user =>
          user.name mustBe name
          user.email mustBe email
        }
      }

      // lowercase email
      val existingEmailUser = user.copy(email= email.toLowerCase)
      // try to create a user with the same email to lowercase
      val result2 = userDAO.insert(existingEmailUser)

      whenReady(result2) { maybeUser =>
        maybeUser mustBe None
      }

    }

    "find a user by id" in {

      val result = userDAO.insert(
          User(None, "Yun", "admin@admin.com", RandomDataGenerator.hiddenPassword.string))

      whenReady(userDAO.findByEmail("admin@admin.com")) { maybeUser =>
        Logger.info("working")
        Logger.info("email => " + maybeUser.get.email)
        maybeUser.isDefined mustBe true
      }
    }

    //    "create a new user" in {
    //      val email = RandomDataGenerator.email
    //      val password = RandomDataGenerator.hiddenPassword
    //      val result = userService.create(email, password)
    //
    //      whenReady(result) { maybeUser =>
    //        Logger.info("working")
    //        maybeUser.map( user =>
    //          user.email.string mustBe email.string)
    //      }
    //    }
    //
    //    "Fail to create a new user when the email already exists" in {
    //      val email = RandomDataGenerator.email
    //      val password = RandomDataGenerator.hiddenPassword
    //
    //      val result = userService.create(email, password)
    //      whenReady(result) { maybeUser =>
    //        Logger.info("working")
    //        maybeUser.map( user =>
    //          user.email.string mustBe email.string)
    //      }
    //
    //      val result2 = userService.create(email.copy(string = email.string.toUpperCase), password)
    //
    //      whenReady(result2) { maybeUser =>
    //        Logger.info("working")
    //        maybeUser mustBe None
    //      }
    //
    //    }
  }
}
