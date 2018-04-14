package models.dao

import java.util.Date
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models._
import play.api.Logger
import play.api.db.{Database}

import scala.concurrent.Future

@javax.inject.Singleton
class BookDAO @Inject()(db: Database, publisherRepository: PublisherDAO)(implicit ec: DatabaseExecutionContext) {

  //private val db = dBApi.database("default")

  private val logger = play.api.Logger(this.getClass)

  // -- Parsers

  /**
    * Parse a Book from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("book.id") ~
      get[String]("book.name") ~
      get[Long]("book.price") ~
      get[Option[String]]("book.author") ~
      get[Option[String]]("book.description") ~
      get[Option[String]]("book.imgKey") ~
      get[Option[Boolean]]("book.reserved") ~
      get[Option[Long]]("book.publisher_id") ~
      get[Option[Long]]("book.user_id") map {
      case id~name~price~author~description~imgKey~reserved~publisherId~userId =>
        Book(id, name, price, author, description, imgKey, reserved, publisherId, userId)
    }
  }

  /**
    * Parse a (Book,Publisher) from a ResultSet
    */
  private val withPublisher = simple ~ (publisherRepository.simple ?) map {
    case book~publisher => (book,publisher)
  }


  // -- Queries

  /**
    * Retrieve a book from the id.
    */
  def findById(id: Long): Future[Option[Book]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from book where id = {id}").on('id -> id).as(simple.singleOpt)
    }
  }(ec)


  def userItems(email: String,
                page: Int = 0,
                orderBy: Int = 1,
                filter: String = "%",
                pageSize: Int = 10): Future[Page[(Book, Option[Publisher])]] = Future {
    db.withConnection { implicit connection =>

      val offset = pageSize * page

      val books = SQL(
        """
        select * from book
        left join publisher
          on book.publisher_id = publisher.id
        where book.user_id = (select id from usuario where usuario.email = {email}) AND book.name like {filter}
        order by {orderBy} nulls last
        limit {pageSize} offset {offset}
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
          select count(*) from book
          left join publisher on book.publisher_id = publisher.id
          where book.user_id = (select id from usuario where usuario.email = {email}) AND book.name like {filter}
        """
      ).on(
        'email -> email,
        'filter -> filter
      ).as(scalar[Long].single)

      Page(books, page, offset, totalRows)
    }
  }(ec)

  /**
    * Return a page of (Book,Publisher).
    *
    * @param page Page to display
    * @param pageSize Number of books per page
    * @param orderBy Book property used for sorting
    * @param filter Filter applied on the name column
    */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[(Book, Option[Publisher])]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val books = SQL(
        """
          select * from book
          left join publisher on book.publisher_id = publisher.id
          where book.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offset,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(withPublisher *)

      val totalRows = SQL(
        """
          select count(*) from book
          left join publisher on book.publisher_id = publisher.id
          where book.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(books, page, offset, totalRows)
    }
  }(ec)

  /**
    * Update a book.
    *
    * @param id The book id
    * @param book The book values.
    */
  def update(id: Long, book: Book) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          update book
          set name = {name}, author = {author}, description = {description}, price = {price}, imgKey = {imgKey}, reserved = {reserved},
              publisher_id = {publisher_id}, user_id = {user_id}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> book.name,
        'author -> book.author,
        'description -> book.description,
        'price -> book.price,
        'imgKey -> book.imgKey,
        'reserved -> book.reserved,
        'publisher_id -> book.publisherId,
        'user_id -> book.userId
      ).executeUpdate()
    }
  }(ec)

  /**
    * Insert a new book.
    *
    * @param book The book values.
    */
  def insert(book: Book) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into book values (
            (select nextval('book_seq')),
            {name}, {price}, {author}, {description}, {imgKey}, {reserved}, {publisher_id}, {user_id}
          )
        """
      ).on(
        'name -> book.name,
        'author -> book.author,
        'description -> book.description,
        'price -> book.price,
        'imgKey -> book.imgKey,
        'reserved -> book.reserved,
        'publisher_id -> book.publisherId,
        'user_id -> book.userId
      ).executeUpdate()
    }
  }(ec)

  /**
    * Delete a book.
    *
    * @param id Id of the book to delete.
    */
  def delete(id: Long) = Future {
    db.withConnection { implicit connection =>
      SQL("delete from book where id = {id}").on('id -> id).executeUpdate()
    }
  }(ec)

}
