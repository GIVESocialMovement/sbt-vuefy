package controllers

import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ AbstractController, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class HomeController @Inject()(controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def index = Action.async {
    Future(Ok(views.html.index("Welcome")))
  }
}
