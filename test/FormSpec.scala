
import models.forms.AppForms._
import models.forms.{BookFormData, DevBookFormData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice._



class FormSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  private val logger = play.api.Logger(this.getClass)

  "addBookForm" should {
    "throw an error with more than limit number of imgs " in {

      val data = DevBookFormData(
        title = "James Joyce",
        author = "Ulysses",
        description = "Brand new",
        price = 10,
        imgKeys = List("picture[0]", "picture[1]", "picture[2]", "picture[3]", "picture[4]", "picture[5]"),
        publisherId = None
      )

      val filledForm = devAddBookForm.fillAndValidate(data)

      logger.info(filledForm.errors.mkString(" : "))

      filledForm.hasErrors mustBe true
    }


  }

  //      Logger.info(registerFormFilled.errors.mkString(", "))
  //      Logger.info(registerFormFilled.data.toString)
  //      Logger.info(registerFormFilled.hasErrors.toString)
  //      //registerFormFilled.bindFromRequest()
  //
  //
  //      registerFormFilled.hasGlobalErrors mustBe true


}
