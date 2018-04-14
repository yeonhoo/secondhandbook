
import akka.stream.Materializer
import commons.PostgresDataHandlerSpec
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{AnyContentAsFormUrlEncoded, MessagesRequest, MultipartFormData, Session}
import play.api.test.Helpers._
//import akka.actor.ActorSystem
//import akka.stream.ActorMaterializer
import controllers.MainController
import models.forms.AppForms._
import org.scalatest.concurrent.ScalaFutures
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.CSRFTokenHelper._
import play.api.{Configuration, Logger}


class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with PostgresDataHandlerSpec with EssentialActionCaller with Writeables {

  implicit val materializer = app.injector.instanceOf[Materializer]
  def homeController = app.injector.instanceOf(classOf[MainController])

  //implicit val sys = ActorSystem("MyTest")
  //implicit val mat = ActorMaterializer()


  "register user action" should {

    "register a new user" in {

      val result = homeController.registerUser(
        FakeRequest().withFormUrlEncodedBody(
          "name" -> "yun",
          "email" -> "yun@yun.com",
          "pw" -> "1234",
          "rePw" -> "1234")
          .withCSRFToken
      )

      status(result) must equal(SEE_OTHER)
      redirectLocation(result) mustBe Some("/books")
      flash(result).get("success") mustBe Some("User yun has been created")
    }

    "fail to create a user with already existing email" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "yun",
        "email" -> "admin@4989.com.br",
        "pw" -> "1234",
        "rePw" -> "1234").withCSRFToken

      val result = homeController.registerUser(request)


      status(result) must equal(BAD_REQUEST)
      flash(result).get("error") mustBe Some("email already exist")

    }

    "fail with email form error" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "yun",
        "email" -> "admin4989.com.br",
        "pw" -> "1234",
        "rePw" -> "1234").withCSRFToken

      val result = homeController.registerUser(request)

      status(result) must equal(BAD_REQUEST)
      flash(result).get("error") mustBe Some("form error")

    }
  }
  "edit action" should {

    val request = FakeRequest().
      withSession("email" -> "admin@4989@com.br", "name" -> "yun", "id" -> "1").withCSRFToken

    "fail when user is not logged" in {
      val result4 = homeController.edit(1)(FakeRequest()).run()(materializer)
      status(result4) must equal(SEE_OTHER)
      redirectLocation(result4) mustBe Some("/login")
    }

    "be able to a user to edit his own book" in {
      val result1 = homeController.edit(1)(request).run()(materializer)
      status(result1) must equal(OK)
    }

    "not be able to a user to edit a book which does not exist" in {
      val result3 = homeController.edit(4)(request).run()(materializer)
      redirectLocation(result3) mustBe Some("/notfound")
      flash(result3).get("notFound") mustBe Some("Book doesn't exist")
    }

    "not be able to a user to edit a book which does not belong to him" in {
      val result2 = homeController.edit(3)(request).run()(materializer)
      status(result2) must equal(SEE_OTHER)
      redirectLocation(result2) mustBe Some("/unauthorized")
      flash(result2).get("unauthorized") mustBe Some("Book Some(3) doesn't belong to you")
    }

  }

  "delete action" should {

    val request = FakeRequest().
      withSession("email" -> "admin@4989@com.br", "name" -> "yun", "id" -> "1")

    "redirect when a user is not logged" in {
      val result4 = homeController.delete(1)(FakeRequest()).run()(materializer)
      status(result4) must equal(SEE_OTHER)
      redirectLocation(result4) mustBe Some("/login")
    }

    "be able to a user delete his own book" in {
      // user_id 1 try to delete his own book(id 1)
      val result = homeController.delete(1)(request).run()(materializer)

      flash(result).get("success") mustBe Some("Book has been deleted")
      status(result) must equal(SEE_OTHER)
      redirectLocation(result) mustBe Some("/books")
    }

    "not be able to delete a book which does not belong to him" in {
      // user_id 1 try to delete the book (id = 3) which doesn't belong to him
      val result = homeController.delete(3)(request).run()(materializer)
      flash(result).get("unAuthorized") mustBe Some("Book doesn't belong to you")
      status(result) must equal(SEE_OTHER)
      redirectLocation(result) mustBe Some("/unauthorized")
    }
    "redirect to NotFound page when try to delete a book that does not exist" in {
      // user_id 1 try to delete a book(id 4) that doesn't exist
      val result = homeController.delete(4)(request).run()(materializer)
      flash(result).get("notFound") mustBe Some("Book was not found")
      status(result) must equal(SEE_OTHER)
      redirectLocation(result) mustBe Some("/notfound")
    }
  }


  "authenticate action" should {

    "fail with wrong form data (short pw length)" in {
      val request =
        FakeRequest().withFormUrlEncodedBody("email" -> "admin@4989.com.br", "password" -> "yun").withCSRFToken

      val result = homeController.authenticate(request)
      contentAsString(result) must include("Invalid Password")
      status(result) must equal(BAD_REQUEST)
    }


    "fail when an user is not registered" in {
      val request =
        FakeRequest().withFormUrlEncodedBody("email" -> "john@doe.com", "password" -> "john").withCSRFToken
      val result = homeController.authenticate(request)

      status(result) must equal(BAD_REQUEST)
      contentAsString(result) must include("It was not possible to find a user")
    }

    "authenticate an already registered user" in {
      val request =
        FakeRequest().withFormUrlEncodedBody("email" -> "admin@4989.com.br", "password" -> "yunn").withCSRFToken

      val result = homeController.authenticate(request)

      status(result) must equal(SEE_OTHER)
      session(result) mustBe Session(Map("email" -> "admin@4989.com.br", "name" -> "yunn", "id" -> "2"))
      redirectLocation(result) mustBe Some("/books")
      flash(result).get("success") mustBe Some("Welcome yunn")


    }
  }


  "add book action" should {

    // form data
    val dataParts = Map(
      "name" -> Seq("Great Gatsby"),
      "price" -> Seq("30"),
      "author" -> Seq("Scott"),
      "description" -> Seq("Great classic")
    )

    // create a temporary file from the already existing file
    val temporaryFileCreator = SingletonTemporaryFileCreator
    val file1 = new java.io.File("/home/yun/Desktop/test/1.jpg")
    val file2 = new java.io.File("/home/yun/Desktop/test/2.jpg")
    val file3 = new java.io.File("/home/yun/Desktop/test/3.jpg")
    val file4 = new java.io.File("/home/yun/Desktop/test/4.jpg")
    val file5 = new java.io.File("/home/yun/Desktop/test/5.jpg")
    val file6 = new java.io.File("/home/yun/Desktop/test/6.jpg")

    val tempFile1 = temporaryFileCreator.create(file1.toPath)
    val tempFile2 = temporaryFileCreator.create(file2.toPath)
    val tempFile3 = temporaryFileCreator.create(file3.toPath)
    val tempFile4 = temporaryFileCreator.create(file4.toPath)
    val tempFile5 = temporaryFileCreator.create(file5.toPath)
    val tempFile6 = temporaryFileCreator.create(file6.toPath)

    // create filePart with temporary file
    val filePart1 = FilePart[TemporaryFile](key = "picture[0]", filename = "1.jpg", contentType = Some("image/jpeg"), ref = tempFile1)
    val filePart2 = FilePart[TemporaryFile](key = "picture[1]", filename = "2.jpg", contentType = Some("image/jpeg"), ref = tempFile2)
    val filePart3 = FilePart[TemporaryFile](key = "picture[2]", filename = "3.jpg", contentType = Some("image/jpeg"), ref = tempFile3)
    val filePart4 = FilePart[TemporaryFile](key = "picture[3]", filename = "4.jpg", contentType = Some("image/jpeg"), ref = tempFile4)
    val filePart5 = FilePart[TemporaryFile](key = "picture[4]", filename = "5.jpg", contentType = Some("image/jpeg"), ref = tempFile5)
    val filePart6 = FilePart[TemporaryFile](key = "picture[5]", filename = "6.jpg", contentType = Some("image/jpeg"), ref = tempFile6)


    "form validation fail when try to upload more than 5 image files" in {
      val data = MultipartFormData(

        dataParts = dataParts,
        files = Seq(filePart1, filePart2, filePart3, filePart4, filePart5, filePart6),
        badParts = Nil
      )

      val request = FakeRequest()
        .withMultipartFormDataBody(data)
        .withSession("email" -> "admin4989@com.br", "name" -> "yun", "id" -> "1")
        .withCSRFToken

      val result = call(homeController.addBook, request)

      //AnyContentAsMultipartFormData
      //val result1 = homeController.updateTest(1)(request)
      //val result1 = homeController.update(1)(request).run()(materializer)
      //val c = contentAsString(result1)

      Logger.info("result content type : " + contentType(result))
      Logger.info("result session : " + session(result))
      Logger.info("result headers : " + headers(result))


      val s = status(result) must equal(OK)
    }

    "add a book with one image file" in {


      val data = MultipartFormData(

        dataParts = dataParts,
        files = Seq(filePart1),
        badParts = Nil
      )
      // create a request of type FakeRequest[AnyContentAsMultipartFormData]
      val request = FakeRequest()
        .withMultipartFormDataBody(data)
        .withSession("email" -> "admin4989@com.br", "name" -> "yun", "id" -> "1")
        .withCSRFToken

// this method below will be implicitly called and provides Writeable[AnyContentAsMultipartFormData] object to `call` method
// `implicit def writeableOf_AnyContentAsMultipartForm : Writeable[AnyContentAsMultipartFormData]`
// `def call[T](action: EssentialAction, req: Request[T])(implicit w: Writeable[T], mat: Materializer): Future[Result]`


      val result1 = call(homeController.addBook, request)

      //AnyContentAsMultipartFormData
      //val result1 = homeController.updateTest(1)(request)
      //val result1 = homeController.update(1)(request).run()(materializer)
      //val c = contentAsString(result1)

      Logger.info("result content type : " + contentType(result1))
      Logger.info("result session : " + session(result1))
      Logger.info("result headers : " + headers(result1))


      val s = status(result1) must equal(OK)

    }
  }

  "update action" should {

    "update a book" in {


      val request = FakeRequest()
        .withSession("email" -> "admin@4989@com.br", "name" -> "yun", "id" -> "1")
        .withFormUrlEncodedBody(
          "name" -> "Hello World",
          "price" -> "1000",
          "author" -> "author updated",
          "description" -> "description updated")
        .withCSRFToken


      val result = call(homeController.update(1), request)

      Logger.info("result content type : " + contentType(result))
      Logger.info("result session : " + session(result))
      Logger.info("result headers : " + headers(result))

      flash(result).get("success") mustBe Some("Book Hello World has been updated")
      status(result) must equal(SEE_OTHER)
      redirectLocation(result) mustBe Some("/books")

    }
  }



  "HomeController" should {


    "simulate that error" in {

      val request = FakeRequest()
        .withSession("email" -> "admin@4989@com.br", "name" -> "yun", "id" -> "1")

      // user_id 1 try to delete his own book(id 1)
      val result1 = homeController.delete(1)(request)

      flash(result1).get("success") mustBe Some("Book has been deleted")
      status(result1) must equal(SEE_OTHER)
      redirectLocation(result1) mustBe Some("/books")

      //val status = status(result)
      //redirectLocation(result) mustBe Some("/books")
    }

    "book form test" in {

      val request = FakeRequest()
        .withSession("email" -> "admin@4989@com.br", "name" -> "yun", "id" -> "1")
        .withFormUrlEncodedBody(
          "name" -> "name updated",
          "price" -> "1000",
          "author" -> "author updated",
          "description" -> "description updated")
        .withCSRFToken


      bookForm.bindFromRequest()(request.withBody()).fold(
        formWithErrors => {

          Logger.info(formWithErrors.data.toString())
          Logger.info(formWithErrors.errors.mkString(" "))
          Logger.info(formWithErrors.errors("").mkString(", "))
          Logger.info(formWithErrors.error("").mkString(", "))
          Logger.info(formWithErrors.globalErrors.mkString(", "))

          1 mustBe 1
        },
        data => {
          Logger.info(s"name => ${data.name}")
          Logger.info(s"price => ${data.price}")
          1 mustBe 1
        }
      )

    }

/**
  * @todo : form data erased
 */


/*    "just test" in {



      val userId1:Long = 1

      val user1 = User(Some(userId1), "User1", "user1@user1.com", "user1")

      val book1 = Book(Some(1), "Name", 1, Some("Author"), Some("Description"),
                      Some("ImgKeys"), Some(true), Some(1), Some(userId1))

      val bookId = 1

      // construct the fake request
      val request = FakeRequest().withSession("id" -> "1")



//      val result: Future[Result] = for {
//        insertUser <- userService.insert(user1)
//        insertBook <- bookService.insert(book1)
//        deleteBook <- homeController.delete(bookId)(request)
//      } yield deleteBook

      val result = homeController.delete(bookId)(FakeRequest())

      status(result) must equal (SEE_OTHER)
    }*/

//    "redirect to the book list on /" in {
//      val result = homeController.index(FakeRequest())
//
//      status(result) must equal(SEE_OTHER)
//      redirectLocation(result) mustBe Some("/books")
//    }

//    "list books on the the first page" in {
//      val result = homeController.list(0, 2, "")(FakeRequest())
//
//      status(result) must equal(OK)
//      contentAsString(result) must include("2 books found")
//    }
//
//    "form error test" in {
//
//
//      val pwNotMatchRequest  = FakeRequest().withFormUrlEncodedBody(
//        "name" -> "yun",
//        "email" -> "yun@yun.com",
//        "pw" -> "1243",
//        "rePw" -> "1233")
//        .withCSRFToken
//
//      homeController.userRegisterForm.bindFromRequest()(pwNotMatchRequest).fold(
//        formWithErrors => {
//          Logger.info("pw not matching error => badRequest")
//          Logger.info(formWithErrors.data.toString())
//          Logger.info(formWithErrors.errors.mkString(" "))
//          Logger.info(formWithErrors.errors("").mkString(", "))
//          Logger.info(formWithErrors.error("").mkString(", "))
//          Logger.info(formWithErrors.globalErrors.mkString(", "))
//          val test = formWithErrors.error("").get
//          test.
//
//        },
//        userData => {
//          Logger.info("user => success => create a new user to DB")
//          Logger.info(s"name => ${userData.name}")
//          Logger.info(s"email => ${userData.email}")
//          Logger.info(s"pw => ${userData.pw}")
//          Logger.info(s"re-pw => ${userData.rePw}")
//        }
//
//      )
//
//      // can't get the error on password not matching
//      val constraintsError  = FakeRequest().withFormUrlEncodedBody(
//        "name" -> "yu",
//        "email" -> "yunyun.com",
//        "pw" -> "124",
//        "rePw" -> "123")
//        .withCSRFToken
//
//      homeController.userRegisterForm.bindFromRequest()(constraintsError).fold(
//        formWithErrors => {
//          Logger.info("constraint errors => badRequest")
//          Logger.info(formWithErrors.data.toString())
//          Logger.info(formWithErrors.errors.mkString(" "))
//          Logger.info(formWithErrors.errors("").mkString(", "))
//          Logger.info(formWithErrors.error("").mkString(", "))
//          Logger.info(formWithErrors.globalErrors.mkString(", "))
//
//        },
//        userData => {
//          Logger.info("user => success => create a new user to DB")
//          Logger.info(s"name => ${userData.name}")
//          Logger.info(s"email => ${userData.email}")
//          Logger.info(s"pw => ${userData.pw}")
//          Logger.info(s"re-pw => ${userData.rePw}")
//        }
//
//      )
//
//      val pwNotMatch = UserRegisterFormData("y", "yun.com", "12344", "12444")
//
//
//      val registerFormFilled = homeController.userRegisterForm.fillAndValidate(pwNotMatch)
//
//      Logger.info(registerFormFilled.errors.mkString(", "))
//      Logger.info(registerFormFilled.data.toString)
//      Logger.info(registerFormFilled.hasErrors.toString)
//      //registerFormFilled.bindFromRequest()
//
//
//      registerFormFilled.hasGlobalErrors mustBe true
//
//    }
//
//    "create new user" in {
//
//      val badResult = homeController.registerNewUser(FakeRequest().withCSRFToken)
//
//      status(badResult) must equal(BAD_REQUEST)
//
//
//      val badPasswordNotMaching = homeController.registerNewUser(
//        FakeRequest().withFormUrlEncodedBody(
//          "name" -> "yun",
//          "email" -> "yun@yun.com",
//          "pw" -> "1234",
//          "rePw" -> "1213")
//          .withCSRFToken
//      )
//
//      status(badPasswordNotMaching) must equal(BAD_REQUEST)
//      //contentAsString(badDateFormat) must include("""<option value="1" selected="selected">Apple Inc.</option>""")
//      //contentAsString(badDateFormat) must include("""<input type="text" id="introduced" name="introduced" value="badbadbad" """)
//      //contentAsString(badDateFormat) must include("""<input type="text" id="name" name="name" value="FooBar" """)
//
//
//      val result = homeController.registerNewUser(
//        FakeRequest().withFormUrlEncodedBody(
//          "name" -> "yun",
//          "email" -> "yun@yun.com",
//          "pw" -> "1234",
//          "rePw" -> "1234")
//          .withCSRFToken
//      )
//
//      status(result) must equal(SEE_OTHER)
//      redirectLocation(result) mustBe Some("/books")
//      flash(result).get("success") mustBe Some("User yun has been created")
//
////      val list = homeController.list_test(0, 2, "FooBar")(FakeRequest())
////
////      status(list) must equal(OK)
////      contentAsString(list) must include("One computer found")
//    }


//
//    "filter computer by name" in {
//      val result = homeController.list(0, 2, "Apple")(FakeRequest())
//
//      status(result) must equal(OK)
//      contentAsString(result) must include("13 computers found")
//    }
//
//    //running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
//    "create new computer" in {
//      val badResult = homeController.save(FakeRequest().withCSRFToken)
//
//      status(badResult) must equal(BAD_REQUEST)
//
//      val badDateFormat = homeController.save(
//        FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "badbadbad", "company" -> "1").withCSRFToken
//      )
//
//      status(badDateFormat) must equal(BAD_REQUEST)
//      contentAsString(badDateFormat) must include("""<option value="1" selected="selected">Apple Inc.</option>""")
//      contentAsString(badDateFormat) must include("""<input type="text" id="introduced" name="introduced" value="badbadbad" """)
//      contentAsString(badDateFormat) must include("""<input type="text" id="name" name="name" value="FooBar" """)
//
//
//      val result = homeController.save(
//        FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "2011-12-24", "company" -> "1").withCSRFToken
//      )
//
//      status(result) must equal(SEE_OTHER)
//      redirectLocation(result) mustBe Some("/computers")
//      flash(result).get("success") mustBe Some("Computer FooBar has been created")
//
//      val list = homeController.list(0, 2, "FooBar")(FakeRequest())
//
//      status(list) must equal(OK)
//      contentAsString(list) must include("One computer found")
//    }
  }
}
