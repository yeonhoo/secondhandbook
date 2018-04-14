package controllers

import java.io.File
import javax.inject.Inject

import models._
import models.dao.{BookDAO, PublisherDAO, UserDAO}
import models.images.Images
import play.api.{Configuration, Logger}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import views.{html, _}
import models.forms.AppForms._
import models.forms._

import scala.concurrent.{ExecutionContext, Future}


class MainController @Inject()(langs: Langs,
                               messagesApi: MessagesApi,
                               images: Images,
                               config: Configuration,
                               userService: UserDAO,
                               bookService: BookDAO,
                               publisherService: PublisherDAO,
                               cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) with Common {

  val availableLangs: Seq[Lang] = langs.availables
  val lang: Lang = langs.availables.head
  //val title: String = messagesApi("home.title")(lang)

  val messages: Messages = MessagesImpl(lang, messagesApi)
  val title: String = messages("home.title")

  def availLangs = Action {
    Ok(availableLangs.mkString(", ") + " title : " + title)
  }

  private val logger = play.api.Logger(this.getClass)

  type LoginForm = Form[(String, String)]


  def index = Action {
    Home
  }

//  def adminIndex = isAuthenticated { userEmail => implicit request =>
//    Ok("Hello " + userEmail)
//  }

  def userinfoIndex = isAuthenticatedAsync { (id, name, email) => implicit request =>
    Future(Ok(s"Hello id: ${id}, name: ${name}, email: ${email} "))
  }


  def nav(implicit user: Option[String], request: MessagesRequestHeader) = html.nav(user)(loginForm)

  def unAuthorized = Action { implicit request =>

    val body = html.error.unAuthorized()
    Ok( html.main(nav)(body) )
  }

  def notFound = Action { implicit request =>

    val body = html.error.notFound()
    Ok( html.main(nav)(body) )
  }

  def imgAddr: String = {
    s"${config.get[String]("aws.s3.server")}/" +
      s"${config.get[String]("aws.s3.bucket")}"
  }

  def itemDetailsPage(id: Long) = Action.async { implicit request =>

    bookService.findById(id).flatMap {
      case Some(book) =>
        publisherService.options.map { options =>
          val body = html.itemDetails(book, imgAddr)
          Ok( html.main(nav)(body) )
        }
      case other =>
        Future( NotFoundPage.flashing("notFound" -> s"Book id : %s was not found".format(id)) )
    }
  }

  def userItems(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>

    userEmail(request).map {
      email =>
        bookService.userItems(email, page, orderBy, filter = ("%" + filter + "%")).map { page =>
          val body = html.userItems(page, orderBy, filter)(imgAddr)
          Ok(html.main(nav)(body))
        }

    }.getOrElse {
      Future ( UnauthorizedPage.flashing("unauthorized" -> "You are not logged") )
    }

  }

  def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>

    bookService.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")).map { page =>
      val body = html.list(page, orderBy, filter)(imgAddr)
      Ok(html.main(nav)(body))
    }
  }

  def registerPage = Action { implicit request =>

    val body = html.registerForm(userRegisterForm)
    Ok( html.main(nav)(body) )
  }

  def registerUser = Action.async { implicit request =>

    userRegisterForm.bindFromRequest.fold(
      formWithErrors => {
        val registerPage = html.registerForm(formWithErrors)
        Future(BadRequest(html.main(nav)(registerPage)).flashing("error" -> "form error"))
      },

      userData => {
        val newUser = User(None, userData.name, userData.email, userData.pw)
        userService.insert(newUser).map{ maybeUser =>
          maybeUser match {
            case Some(user) =>
              Home.flashing("success" -> "User %s has been created".format(user.name))

            case None =>
              val registerPage = html.registerForm(userRegisterForm.withGlobalError("email already exists", "what"))
              BadRequest(html.main(nav)(registerPage)).flashing("error" -> "email already exist")
          }
        }
      }
    )
  }

  def login = Action { implicit request =>

    val body = html.login(loginForm)
    Ok(html.main(nav)(body))
  }

  def logout = Action {
    Home.withNewSession.flashing("success" -> "You've been logged out")
  }

  def authenticate = Action.async { implicit request =>

    loginForm.bindFromRequest.fold(

      formWithErrors => {
        val loginFormPage = html.login(formWithErrors)
        Future(BadRequest(html.main(nav)(loginFormPage)))
      },

      user => {
        userService.authenticate(user._1, user._2).map { maybeUser =>
          maybeUser.map { user =>
            Home.withSession("email" -> user.email, "name" -> user.name, "id" -> user.id.get.toString)
              .flashing("success" -> "Welcome %s".format(user.name))
          }.getOrElse { // authentication error
            val loginFormPage = html.login(loginForm.withGlobalError("It was not possible to find a user"))
            BadRequest(html.main(nav)(loginFormPage))
          }
        }
      }
    )
  }

  def edit(id: Long) = isAuthenticatedAsync { (userId, _, _) => implicit request =>

      bookService.findById(id).flatMap {
        case Some(book) =>

          val bookUserId = book.userId.get
          // usuario que esta logado deve ser o dono do anuncio
          if (bookUserId == userId.toLong) {
            publisherService.options.map { options =>
              val editFormPage = html.editForm(id, bookForm.fill(book), options)
              Ok(html.main(nav)(editFormPage))
            }
          } else {
            Future ( UnauthorizedPage.flashing("unauthorized" -> "Book %s doesn't belong to you".format(book.id)) )
          }
        case other =>
          Future( NotFoundPage.flashing("notFound" -> "Book doesn't exist") )
      }
  }

  def delete(id: Long) = isAuthenticatedAsync { (userId, _, _) =>

    implicit request =>
      bookService.findById(id).flatMap {
        case Some(book) if book.userId.get == userId.toLong =>
          bookService.delete(id).map { _ =>
            Home.flashing("success" -> "Book has been deleted")
          }
        case Some(_) =>
          Future.successful(UnauthorizedPage.flashing("unAuthorized" -> "Book doesn't belong to you"))
        case _ =>
          Future.successful(NotFoundPage.flashing("notFound" -> "Book was not found"))
      }
  }

  // por enquanto vou deixar option assim e nao usar no view
  // dps trato ele como uma categoria
  def addBookPage = isAuthenticatedAsync { (_, _, _) => implicit request =>
      publisherService.options.map { options =>
        val addBookPage = html.addBookForm(addBookForm, options)
        Ok(html.main(nav)(addBookPage))
      }
  }


  def update(id: Long) = isAuthenticatedAsync { (userId, _, _) =>
    implicit request =>

      logger.warn("data : " + bookForm.bindFromRequest.data.mkString(", "))
      logger.warn("errors : " + bookForm.bindFromRequest.errors.mkString(", "))


      bookForm.bindFromRequest.fold(
        formWithErrors => {
          publisherService.options.map { options =>
            val editFormPage = html.editForm(id, formWithErrors, options)
            BadRequest(html.main(nav)(editFormPage))
          }
        },
        data => {
          bookService.findById(id).flatMap {
            // does book belongs to the user?
            case Some(book) if book.userId.get == userId.toLong =>
              val updatedBook =
                book.copy(id= None, name= data.name, price= data.price, description= data.description)
              bookService.update(id, updatedBook).map { _ =>
                Home.flashing("success" -> "Book %s has been updated".format(book.name))
              }
            case Some(_) =>
              Future (
                UnauthorizedPage.flashing("unauthorized" -> "Book doesn't belong to you") )
            case _ =>
              Future( NotFoundPage.flashing("notFound" -> "Book doesn't exist") )
          }

        }
      )
  }


  def addBook = isAuthenticatedAsync { (userId, _, _) => implicit request =>


    request.body.asMultipartFormData.get.files.foreach{ file =>
      logger.warn("body data asMultipartFormData Files: " + file)

    }
      logger.warn("data : " + addBookForm.bindFromRequest.data.mkString(", "))
      logger.warn("errors : " + addBookForm.bindFromRequest.errors.mkString(", "))


      addBookForm.bindFromRequest.fold(
        formWithErrors => publisherService.options.map { options =>

          val addBookPage = html.addBookForm(formWithErrors, options)
          BadRequest(html.main(nav)(addBookPage))
        },
        {
          case BookFormData(name, price, author, description, imgs, reserved, publisher) => {

            val formData = request.body.asMultipartFormData
            //TODO: filtering files is not good just by filename.isEmpty, cuz an attacker can enter filename without actually submitting the file
            //TODO: validate if the file is a valid image
            val imgKeys = formData.get.files.filter(!_.filename.isEmpty) map { filePart =>

              logger.warn("Filepart filename => " + filePart.filename)
              val newFile = File.createTempFile("temp-uploaded-", filePart.filename)
              filePart.ref.moveTo(newFile, true)

              images.processImage(newFile)
            }
            // separates each imgkey with |
            val imageKeys = if (imgKeys.size == 0) None else Some(imgKeys.mkString("|"))

            val newBook = Book(None, name, price, author, description, imageKeys, reserved, publisher, Some(userId.toLong))

            bookService.insert(newBook).map { _ =>
              Home.flashing("success" -> "Book %s has been created".format(newBook.name))
            }
          }
        }
      )
  }
}
