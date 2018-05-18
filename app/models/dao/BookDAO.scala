package models.dao

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.domain.{Book, Page, Publisher}
import play.api.db.Database

import scala.concurrent.Future

@javax.inject.Singleton
class BookDAO @Inject()(db: Database, publisherDAO: PublisherDAO)(implicit ec: DatabaseExecutionContext) {

  private val simple = {
    get[Option[Long]]("book.id") ~
      get[String]("book.title") ~
      get[String]("book.author") ~
      get[String]("book.description") ~
      get[Long]("book.price") ~
      get[Option[String]]("book.imgs") ~
      get[Int]("book.status") ~
      get[Int]("book.up_count") ~
      get[Int]("book.down_count") ~
      get[Long]("book.user_account_id") ~
      get[Option[Long]]("book.publisher_id") map {
      case id~title~author~description~price~imgKeys~status~upCount~downCount~userId~publisherId =>
        Book(id, title, author, description, price, imgKeys, status, upCount, downCount, userId, publisherId)
    }
  }

  /**
    * Parse a (Book,Publisher) from a ResultSet
    */
  private val withPublisher = simple ~ (publisherDAO.simple ?) map {
    case book~optionPublisher => (book,optionPublisher)
  }


  def insert(book: Book) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO book VALUES(
            (select nextval('book_seq')),
            {title}, {author}, {description}, {price}, {imgKeys}, {status},
            {up_count}, {down_count}, {user_id}, {publisher_id})
          RETURNING id, title, author, description, price, imgs,
            status, up_count, down_count, user_account_id, publisher_id
        """
      ).on(
        'title -> book.title,
        'author -> book.author,
        'description -> book.description,
        'price -> book.price,
        'imgKeys -> book.imgKeys,
        'status -> book.status,
        'up_count -> book.upCount,
        'down_count -> book.downCount,
        'user_id -> book.userId,
        'publisher_id -> book.publisherId
      ).as(simple.singleOpt)
    }
  }

  def update(id: Long, book: Book): Future[Option[Book]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          UPDATE book
          SET title = {title}, author = {author}, description = {description},
              price = {price}, imgs = {imgKeys}, up_count = {up_count}, down_count = {down_count},
              publisher_id = {publisher_id}, user_account_id = {user_id}
          WHERE id = {id}
          RETURNING id, title, author, description, price, imgs,
                    status, up_count, down_count, user_account_id, publisher_id
         """
      ).on(
        'id -> book.id,
        'title -> book.title,
        'author -> book.author,
        'description -> book.description,
        'price -> book.price,
        'imgKeys -> book.imgKeys,
        'status -> book.status,
        'up_count -> book.upCount,
        'down_count -> book.downCount,
        'user_id -> book.userId,
        'publisher_id -> book.publisherId
      ).as(simple.singleOpt)
    }
  }

  def delete(id: Long) = Future {
    db.withConnection { implicit connection =>
      SQL("DELETE FROM book WHERE id = {id}").on('id -> id).executeUpdate()
    }
  }

  def list(page: Int = 0,
           pageSize: Int = 10,
           orderBy: Int = 1,
           filter: String = "%"): Future[Page[(Book, Option[Publisher])]] =
    Future {

      val offset = pageSize * page

      db.withConnection { implicit connection =>

        val books = SQL(
          """
            SELECT * FROM book
              LEFT JOIN publisher ON book.publisher_id = publisher.id
            WHERE book.title LIKE {filter}
            ORDER BY {orderBy} NULLS LAST
            LIMIT {pageSize} OFFSET {offset}
        """
        ).on(
          'pageSize -> pageSize,
          'offset -> offset,
          'filter -> filter,
          'orderBy -> orderBy
        ).as(withPublisher *)

        val totalRows = SQL(
          """
            SELECT count(*) FROM book
              LEFT JOIN publisher ON book.publisher_id = publisher.id
            WHERE book.title LIKE {filter}
        """
        ).on(
          'filter -> filter
        ).as(scalar[Long].single)

        Page(books, page, offset, totalRows)
      }
    }


  def userBooks(email: String, page: Int = 0, orderBy: Int = 1,
                filter: String = "%", pageSize: Int = 10): Future[Page[(Book, Option[Publisher])]] =
    Future {
      db.withConnection { implicit connection =>

        val offset = pageSize * page

        val books = SQL(
          """
            SELECT * FROM book
              LEFT JOIN publisher ON book.publisher_id = publisher.id
            WHERE book.user_account_id = (SELECT id
                                  FROM user_account
                                  WHERE user_account.email = {email})
              AND book.title LIKE {filter}

            ORDER BY {orderBy} NULLS LAST
            LIMIT {pageSize} OFFSET {offset}
        """
        ).on(
          'pageSize -> pageSize,
          'offset -> offset,
          'filter -> filter,
          'orderBy -> orderBy,
          'email -> email
        ).as(withPublisher *)

        val totalRows = SQL(
          """
            SELECT count(*)
            FROM book
              LEFT JOIN publisher ON book.publisher_id = publisher.id
            WHERE book.user_account_id = (SELECT id
                                  FROM user_account
                                  WHERE user_account.email = {email})
              AND book.title LIKE {filter}
          """
        ).on(
          'email -> email,
          'filter -> filter
        ).as(scalar[Long].single)

        Page(books, page, offset, totalRows)
      }
    }

  def findById(id: Long): Future[Option[Book]] = Future {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM book WHERE id = {id}").on('id -> id).as(simple.singleOpt)
    }
  }

}
