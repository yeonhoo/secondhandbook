package controllers

import models.forms.AppForms.loginForm
import play.api.libs.streams.Accumulator
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext, Future}

trait Common extends MessagesAbstractController {

  implicit protected def userName(implicit request: RequestHeader) = request.session.get("name")
  protected def userEmail(request: RequestHeader) = request.session.get("email")
  protected def userId(request: RequestHeader) = request.session.get("id")
  protected def userInfo (request: RequestHeader) = {
    val id = request.session.get("id")
    val name = request.session.get("name")
    val email = request.session.get("email")

    (id, name , email) match {
      case (Some(id), Some(name), Some(email)) => Some(id, name, email)
      case _ => None
    }
  }

  private val logger = play.api.Logger(this.getClass)

  val Home = Redirect(routes.MainController.list(0, 2, ""))

  val UnauthorizedPage = Redirect(routes.MainController.unAuthorized)

  val NotFoundPage = Redirect(routes.MainController.notFound)

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.MainController.login)

  def isAuthenticatedAsync(f: => (String, String, String) => MessagesRequest[AnyContent] => Future[Result]) = Security.Authenticated(userInfo, onUnauthorized) { user =>

    Action.async {request =>
      f(user._1,user._2,user._3)(request)
    }
  }


}
