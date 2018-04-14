
import models.forms.AppForms._
import models.forms.BookFormData
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice._




class FormSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  private val logger = play.api.Logger(this.getClass)

  "addBookForm" should {
    "throw an error with more than limit number of imgs " in {

      val data = BookFormData(
        name = "James Joyce",
        price= 10,
        author= Some("Ulysses"),
        description= Some("Brand new"),
        imgKey= List("picture[0]", "picture[1]", "picture[2]", "picture[3]", "picture[4]", "picture[5]"),
        reserved= Some(true),
        publisherId= None
      )

      val filledForm = addBookForm.fillAndValidate(data)

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
