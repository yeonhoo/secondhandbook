package models.forms

import models.Book
import play.api.data.{Form, Forms}
import play.api.data.Forms._


case class UserRegisterFormData(name: String, email: String, pw: String, rePw: String)

case class BookFormData(name: String, price: Long, author: Option[String],
                        description:Option[String], imgKey: List[String],
                        reserved: Option[Boolean], publisherId: Option[Long])

object AppForms {

  // i think this form is used mainly when a Book object is retrieved from DB
  val bookForm = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "name" -> nonEmptyText,
      "price" -> longNumber,
      "author" -> optional(text),
      "description" -> optional(text),
      "imgKey" -> optional(text),
      "reserved" -> optional(boolean),
      "publisher" -> optional(longNumber),
      "user" -> optional(longNumber)
    )(Book.apply)(Book.unapply)
  )

  // lets create another Form which will be used to ADD new book

  val addBookForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "price" -> longNumber,
      "author" -> optional(text),
      "description" -> optional(text),
      // i should test this in test frame, this particular case
      // when user try to submit more than 5 pics, which is considered as an attack.
      "pictures" -> Forms.list(text).verifying("more than 5 pictures detected", list => list.size <= 5),
      "reserved" -> optional(boolean),
      "publisher" -> optional(longNumber),
    )(BookFormData.apply)(BookFormData.unapply)
  )

  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 4, maxLength = 20)
    ) /*verifying ("Invalid email or password", result => result match {
      case (email, password) => Await.result(userService.authenticate(email, password), 1 second).isDefined
    })*/
  )

  val userRegisterForm = Form(
    mapping(
      "name" -> nonEmptyText(minLength = 3, maxLength = 30),
      "email" -> email,
      "pw" -> nonEmptyText(minLength = 4, maxLength = 20),
      "rePw" -> nonEmptyText(minLength = 4, maxLength = 20)
    )(UserRegisterFormData.apply)(UserRegisterFormData.unapply)
      verifying ("Passwords are not matching", fields => fields match {
      case UserRegisterFormData(_,_, pw, rePw) => pw == rePw
    })
  )
}
