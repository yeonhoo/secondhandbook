package dao

import akka.actor.ActorSystem
import commons.{PostgresDataHandlerSpec, PostgresDevMode}
import models.dao.{CommentDAO, DatabaseExecutionContext}
import models.domain.Comment
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._


class CommentPostgresDAOSpec extends PlaySpec with ScalaFutures with PostgresDataHandlerSpec with PostgresDevMode {
  val sys = ActorSystem("test")
  val ec = new DatabaseExecutionContext(sys)

  lazy val commentDAO = new CommentDAO(database)(ec)

  val comment = Comment(content = "Hello", userId = 1, status = 1, bookId = 1)

  "Comment service" should {

    "insert a comment" in {
      val result = commentDAO.insert(comment)

      whenReady(result) { updatedRowCount =>
        updatedRowCount mustBe 1
      }
    }
    "retrieve comment list" in {
      val result = commentDAO.comments(1)

      whenReady(result) { comments =>
        comments.size mustBe 5
      }
    }
  }
}
