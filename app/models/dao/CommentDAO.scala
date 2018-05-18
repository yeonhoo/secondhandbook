package models.dao

import java.time.LocalDate
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.domain.Comment
import play.api.db.Database

import scala.concurrent.Future



@javax.inject.Singleton
class CommentDAO @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {

  private val simple = {
    get[Option[Long]]("comment.id") ~
      get[String]("comment.content") ~
      get[LocalDate]("comment.created") ~
      get[Long]("comment.user_account_id") ~
      get[Long]("comment.status") ~
      get[Long]("comment.book_id") map {
      case id~content~created~userId~status~bookId =>
        Comment(id, content, Some(created), userId, status, bookId)
    }
  }

  def insert(comment: Comment) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO comment(content, user_account_id, status, book_id)
          VALUES( {content}, {user_account_id}, {status}, {book_id} )
        """
      ).on(
        'content -> comment.content,
        'user_account_id -> comment.userId,
        'status -> comment.status,
        'book_id -> comment.bookId
      ).executeUpdate()
    }
  }

  def comments(bookId: Long) = Future {
    db.withConnection { implicit connection =>

      SQL(
        """
         SELECT * FROM comment WHERE book_id = {bookId}
         ORDER BY created
      """
      ).on('bookId -> bookId).as(simple *)
    }
  }

}
