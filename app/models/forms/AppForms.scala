package models.forms

import models.domain.Book
import play.api.data.{Form, Forms}
import play.api.data.Forms._


case class UserRegisterFormData(name: String, email: String, pw: String, rePw: String)


case class DevBookFormData(title: String, author: String, description: String, price: Long,
                           imgKeys: List[String],
                           publisherId: Option[Long])

case class BookFormData(name: String, price: Long, author: Option[String],
                        description:Option[String], imgKey: List[String],
                        reserved: Option[Boolean], publisherId: Option[Long])

object AppForms {
  val devBookForm = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "description" -> text,
      "price" -> longNumber,
      "imgKeys" -> optional(text),
      "status" -> number,
      "upCount" -> number,
      "downCount" -> number,
      "userId" -> longNumber,
      "publisher" -> optional(longNumber)
    )(Book.apply)(Book.unapply)
  )
  // lets create another Form which will be used to ADD new book

  val devAddBookForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> longNumber,
      // i should test this in test frame, this particular case
      // when user try to submit more than 5 pics, which is considered as an attack.
      "pictures" -> Forms.list(text).verifying("more than 5 pictures detected", list => list.size <= 5),
      "publisher" -> optional(longNumber),
    )(DevBookFormData.apply)(DevBookFormData.unapply)
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

  val addCommentForm = Form(
    single(
      "content" -> nonEmptyText
    )
  )
}
