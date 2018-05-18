package dao

import akka.actor.ActorSystem
import commons.{PostgresDataHandlerSpec, PostgresDevMode, RandomDataGenerator}
import models.dao.{DatabaseExecutionContext, UserDAO}
import models.domain.User
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._


class UserPostgresDAOSpec extends PlaySpec with ScalaFutures with PostgresDataHandlerSpec with PostgresDevMode  {


  // pricesa inicializar primeiro com "dev.conf" para inicializar o db "devmode"
  // eu nao deveria nem precisar criar "devmode" na localhost
  // pq o teste eh para conectar no db da docker e nao na localhost


  implicit val sys = ActorSystem("test")
  //val ec: ExecutionContext = sys.dispatchers.lookup("database.dispatcher")
  val ec = new DatabaseExecutionContext(sys)


//  val application = new GuiceApplicationBuilder()
//    .loadConfig(Configuration(ConfigFactory.load("dev.conf"))).build()
//
//  implicit val ec: DatabaseExecutionContext = application.injector.instanceOf(classOf[DatabaseExecutionContext])

  lazy val userDAO = new UserDAO(database)(ec)

  // user object
  val name = "Yun"
  val email = RandomDataGenerator.email.string
  val pw = RandomDataGenerator.hiddenPassword.string
  val status = 1 // VERIFIED

  val user = User(None, name, email, pw, status)


  "User service" should {

    "authenticate a User with the email and pw" in {

    }
    "change the password" in {
      val email = "admin@4989.com"
      val newPassword = "1234"
      val result = userDAO.changePassword(email, newPassword)

      whenReady(result) { updatedRowCount =>
        updatedRowCount mustBe 1
      }
    }

    "create a new user" in {

      val result = userDAO.insert(user)

      whenReady(result) { maybeUser =>

        maybeUser.map { user =>
          user.name mustBe name
          user.email mustBe email
          user.status mustBe 1
          user.pw mustBe ""
        }
      }
    }

    "fail to create a new user with already existing email" in {

      //insert an User to DB
      val result = userDAO.insert(user)

      whenReady(result) { maybeUser =>
        maybeUser.map { user =>
          user.name mustBe name
          user.email mustBe email
        }
      }

      // create an user with lowercase email
      val existingEmailUser = user.copy(email= email.toLowerCase)
      // try to insert a User with the lowercased email
      val noneResult = userDAO.insert(existingEmailUser)

      whenReady(noneResult) { maybeUser =>
        maybeUser mustBe None
      }

    }

    "find a user by id" in {

      // could be done either by using for-comprehensions or flatmap
      // option 1
      val futureResult = for {
        insertUser <-userDAO.insert(user)
        findEmail <- userDAO.findByEmail(email)
      } yield findEmail

      // option 2
      //val result = userDAO.insert(user) flatMap { _ => userDAO.findByEmail(email)}
      whenReady(futureResult) { maybeUser =>
        maybeUser.isDefined mustBe true
      }
    }
  }
}
