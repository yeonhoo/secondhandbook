package dao

import akka.actor.ActorSystem
import commons.{PostgresDataHandlerSpec, PostgresDevMode}
import models.dao.{BookDAO, DatabaseExecutionContext, PublisherDAO}
import models.domain.Book
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._


class BookPostgresDAOSpec extends PlaySpec with ScalaFutures with PostgresDataHandlerSpec with PostgresDevMode  {

// when mixed with GuiceOneAppPerSuite starts all application and can use like :
// implicit val ec: DatabaseExecutionContext = app.injector.instanceOf(classOf[DatabaseExecutionContext])
// lazy val userService = new UserDAO(database)(ec)

  val sys = ActorSystem("test")
  val ec = new DatabaseExecutionContext(sys)

  lazy val publisherDAO = new PublisherDAO(database)(ec)
  lazy val bookDAO = new BookDAO(database, publisherDAO)(ec)

  val title = "Scala Test"
  val author = "Yun"
  val description = "About testing book"
  val price = 10
  val imgKeys = Some("ABcd")
  val status = 1 // VERIFIED
  val upCount,downCount = 0
  val userId = 1
  val publisherId = Some(1.toLong)

  val book = Book(None, title, author, description, price, imgKeys, status,
    upCount, downCount, userId, publisherId)

  "Book service" should {

    "retrieve a list within page" in {
      val result = bookDAO.list()
      whenReady(result) { Page =>
        Page.items.size mustBe 6
      }
    }

    "retrieve books that pertain to a user" in {
      val result = bookDAO.userBooks(email = "admin@4989.com")
      whenReady(result) { Page =>
        Page.items.size mustBe 2
      }
    }

    "find by id" in {

      val result = bookDAO.findById(1)

      //whenReady(result) { _.filter(_ => true) mustBe a [Some[_]]
      whenReady(result) { maybeBook => maybeBook.filter(book => true) mustBe a [Some[_]]

      }
    }

    "create a new book" in {

      val result = bookDAO.insert(book)

      whenReady(result) { _.map {

          _.id mustBe a [Some[_]]
          //_.id mustBe Some(1)
        }
      }

    }

    "update a book" in {

      val updatedBook = book.copy(title= "title updated")
      val result = bookDAO.insert(book) flatMap { _ => bookDAO.update(1, updatedBook)}

      whenReady(result) { maybeBook =>
        maybeBook.map { book => book.title mustBe "title updated"}
      }
    }


    "delete a book" in {

      // insert and then delete
      val result = bookDAO.insert(book) flatMap { maybeBook =>
        val id = maybeBook.get.id.get
        bookDAO.delete(id)
      }

      whenReady(result) { deletedRowNumber => deletedRowNumber mustBe 1 }
    }

  }
}
