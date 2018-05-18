package models.dao

import javax.inject.Inject
import anorm.SqlParser.{str, _}
import anorm._
import models.domain.User
import org.postgresql.util.PGobject
import play.api.db.Database
import scala.concurrent.Future


@javax.inject.Singleton
class UserDAO @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {

  private val user = {
    get[Option[Long]]("user_account.id") ~
      get[String]("user_account.name") ~
      get[String]("user_account.email") ~
      get[String]("user_account.pw") ~
      get[Int]("user_account.status") map {
      case id ~ name ~ email ~ pw ~ status =>
        User(id, name, email, pw, status)
    }
  }

  /**
    * Parse a User with empty password
    */
  private val userWithoutPassword = (long("id") ~ str("name") ~ str("email")(citextToString) ~ int("status")).map {
    case id ~ name ~ email ~ status => User(Some(id), name, email, "", status)
  }


  private def citextToString: Column[String] = Column.nonNull { case (value, meta) =>
    val MetaDataItem(qualified, _, clazz) = meta
    value match {
      case str: String => Right(str)
      case obj: PGobject if "citext" equalsIgnoreCase obj.getType => Right(obj.getValue)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to String for column $qualified, class = $clazz"))
    }
  }

  def authenticate(email: String, pw: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL("SELECT id, name, email, status FROM user_account WHERE email = {email} AND pw = {pw}")
        .on('email -> email, 'pw -> pw).as(userWithoutPassword singleOpt)
    }
  }

  def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL("SELECT id, name, email, status FROM user_account WHERE email = {email}")
        .on('email -> email).as(userWithoutPassword singleOpt)
    }
  }

  def changePassword(email: String, newPassword: String) = Future {
    db.withConnection { implicit c =>
      SQL("UPDATE user_account set pw = {newPassword} WHERE email = {email}").on(
        'newPassword -> newPassword, 'email -> email).executeUpdate()
    }
  }

  def insert(user: User): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into user_account(name, email, status, pw)
          values ({name}, {email}, {status}, {pw})
          ON CONFLICT (email) DO NOTHING
          RETURNING id, name, email, status
        """
      ).on(
        'name -> user.name,
        'email -> user.email,
        'pw -> user.pw,
        'status -> user.status
      ).as(userWithoutPassword.singleOpt)
    }
  }
}