package models

import org.mindrot.jbcrypt.BCrypt

case class User(id: Option[Long] = None, // id can be None?
                name: String,
                email: String,
                pw: String)


case class UserPassword(string: String) extends AnyVal

class UserHiddenPassword private (val string: String) extends AnyVal

object UserHiddenPassword {
  def fromPassword(userPassword: UserPassword): UserHiddenPassword = {
    val string = BCrypt.hashpw(userPassword.string, BCrypt.gensalt())
    new UserHiddenPassword(string)
  }

  /**
    * This method should be used only to wrap a password retrieved from
    * the database, otherwise use [[fromPassword]] method.
    */
  def fromDatabase(string: String): UserHiddenPassword = {
    new UserHiddenPassword(string)
  }
}

case class UserEmail(string: String)



//TODO: define new User model

//case class Userx(id: UserId, email: UserEmail)
//
//
//case class UserId(string: String) extends AnyVal
//
//object UserId {
//
//  def create: UserId = UserId(RandomIdGenerator.stringId)
//
//}
//

