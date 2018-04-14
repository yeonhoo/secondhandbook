package models.dao

import javax.inject.{Inject}

import anorm.SqlParser._
import anorm._
import models._
import play.api.db.{DBApi, Database}
import org.postgresql.util.PGobject

import scala.concurrent.Future


@javax.inject.Singleton
class UserDAO @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {

  //private val db = dbapi.database("default")

  /**
    * Parse a User from a ResultSet
    */
  private val user = {
    get[Option[Long]]("usuario.id") ~
      get[String]("usuario.name") ~
      get[String]("usuario.email") ~
      get[String]("usuario.pw") map {
      case id~name~email~pw =>
        User(id, name, email, pw)
    }
  }

  private def citextToString: Column[String] = Column.nonNull { case (value, meta) =>
    val MetaDataItem(qualified, _, clazz) = meta
    value match {
      case str: String => Right(str)
      case obj: PGobject if "citext" equalsIgnoreCase obj.getType => Right(obj.getValue)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to String for column $qualified, class = $clazz"))
    }
  }

  val u_parser = (long("id") ~ str("name") ~ str("email")(citextToString)).map {
    case id ~ name ~ email => User(Some(id), name, email, "")
  }


  def authenticate(email: String, pw: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL("SELECT id, name, email FROM usuario WHERE email = {email} AND pw = {pw}").on('email -> email, 'pw -> pw).as(u_parser singleOpt)
    }
  }(ec)

  def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL("SELECT * FROM usuario WHERE email = {email}").on('email -> email).as(user singleOpt)
    }
  }

  def changePassword(email: String, newPasswd: String) = Future {
    db.withConnection { implicit c =>
      SQL("UPDATE usuario set pw = {pw} WHERE email = {email}").on(
        'pw -> newPasswd, 'email -> email).executeUpdate()
    }
  }



  def insert(user: User): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into usuario(name, email, pw)
          values ({name}, {email}, {pw})
          ON CONFLICT (email) DO NOTHING
          RETURNING id, name, email
        """
      ).on(
        'name -> user.name,
        'email -> user.email,
        'pw -> user.pw
      ).as(u_parser.singleOpt)
    }
  }(ec)

//TODO
//  def create(email: UserEmail, password: UserHiddenPassword): Future[Option[Userx]] =  Future {
//    db.withConnection { implicit connection =>
//
//      val userId = UserId.create
//      val userMaybe = SQL(
//        """
//          |INSERT INTO userx (user_id, email, password)
//          |VALUES ({user_id}, {email},{password})
//          |ON CONFLICT (email) DO NOTHING
//          |RETURNING user_id, email
//        """.stripMargin
//      ).on(
//        "user_id" -> userId.string,
//        "email" -> email.string,
//        "password" -> password.string
//      ).as(parseUser.singleOpt)
//
//      userMaybe
//    }
//  }

}

