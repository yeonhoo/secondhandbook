
  import play.api.test._
  import org.junit.runner.RunWith
  import org.specs2.runner.JUnitRunner
  import play.api.libs.Files.SingletonTemporaryFileCreator
  import java.io.File
  import java.nio.file.{Path, Paths, Files => JFiles}

  import controllers.routes
  import play.api.mvc.MultipartFormData.FilePart
  import play.api.mvc._

  @RunWith(classOf[JUnitRunner])
  class FileUploadSpec extends AbstractController(Helpers.stubControllerComponents()) with PlaySpecification {
    import scala.concurrent.ExecutionContext.Implicits.global

    "A scala file upload" should {

      "upload file" in new WithApplication {
        // cria um empty file in default temp file directorty - tmpFile (path, name)
        val tmpFile = JFiles.createTempFile(null, null)
        // escreve ao tmpFile o conteudo "hello"
        // o tipo do tmpFile eh path e nao um arquivo concreto
        writeFile(tmpFile, "hello")

        // cria o diretorio
        new File("/tmp/picture").mkdirs()

        //cria um file com path
        val uploaded = new File("/tmp/picture/formuploaded")
        uploaded.delete()

        val parse = app.injector.instanceOf[PlayBodyParsers]
        val Action = app.injector.instanceOf[DefaultActionBuilder]

        //#upload-file-action
        def upload = Action(parse.multipartFormData) { request =>
          request.body.file("picture").map { picture =>

            // only get the last part of the filename
            // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system

            // get the "path of the file" and take just the "name part" of it
            val filename = Paths.get(picture.filename).getFileName

            // move the picture to the specified path overwriting the existing file
            picture.ref.moveTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
            Ok("File uploaded")
          }.getOrElse {

            Redirect(routes.ScalaFileUploadController.index).flashing(
              "error" -> "Missing file")
          }
        }

        //#upload-file-action
        val temporaryFileCreator = SingletonTemporaryFileCreator

        // create a file from a already created empty temp file
        // this creates a TemporaryFile type instance from Path
        val tf = temporaryFileCreator.create(tmpFile)
        val request = FakeRequest().withBody(
          MultipartFormData(
            Map.empty,
            Seq(FilePart("picture", "formuploaded", None, tf)), // formuploaded is the name of the file
            Nil
          )
        )
        testAction(upload, request)
        // moves the file to the path and overwrites to the file "/tmp/picture/formuploaded"
        // "uploaded" holds the link to the path "/tmp/picture/formuploaded" and delete the file in this path
        uploaded.delete()
        success
      }

      "upload file directly" in new WithApplication {

        // cria um empty file in default temp file directorty - tmpFile (path, name)
        //val tmpFile = JFiles.createTempFile(null, null)
        // escreve ao tmpFile o conteudo "hello"
        // o tipo do tmpFile eh path e nao um arquivo concreto

        val tmpFile = Paths.get("/tmp/picture/tmpuploaded")
        writeFile(tmpFile, "hello")

        new File("/tmp/picture").mkdirs()
        val uploaded = new File("/tmp/picture/uploaded")
        uploaded.delete()

        val temporaryFileCreator = SingletonTemporaryFileCreator

        // 1) creates a Path with content "hello"
        // 2) creates a TemporaryFile with that Path

        val tf = temporaryFileCreator.create(tmpFile)

        // before I have created a request explicitly specifying the "name" of the file
        // in this request, the file it self is named file "tmpuploaded"
        val request = FakeRequest().withBody(tf)

        val controllerComponents = app.injector.instanceOf[ControllerComponents]

        // dessa vez o parser eh o do tipo TemporaryFile
        testAction(new controllers.ScalaFileUploadController(controllerComponents).upload, request)

        uploaded.delete()
        success
      }
    }

    private def testAction[A](action: Action[A], request: => Request[A] = FakeRequest(), expectedResponse: Int = OK) = {
      val result = action(request)
      status(result) must_== expectedResponse
    }

    def writeFile(file: File, content: String): Path = {
      writeFile(file.toPath, content)
    }

    def writeFile(path: Path, content: String): Path = {
      JFiles.write(path, content.getBytes)
    }

  }
//  package controllers {
//
//    class ScalaFileUploadController(controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {
//
//      //#upload-file-directly-action
//      def upload = Action(parse.temporaryFile) { request =>
//        request.body.moveTo(Paths.get("/tmp/picture/uploaded"), replace = true)
//        Ok("File uploaded")
//      }
//      //#upload-file-directly-action
//
//      def index = Action { request =>
//        Ok("Upload failed")
//      }
//
//      //#upload-file-customparser
//      type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]
//
//      def handleFilePartAsFile: FilePartHandler[File] = {
//        case FileInfo(partName, filename, contentType) =>
//          val perms = java.util.EnumSet.of(OWNER_READ, OWNER_WRITE)
//          val attr = PosixFilePermissions.asFileAttribute(perms)
//          val path = JFiles.createTempFile("multipartBody", "tempFile", attr)
//          val file = path.toFile
//          val fileSink = FileIO.toPath(path)
//          val accumulator = Accumulator(fileSink)
//          accumulator.map { case IOResult(count, status) =>
//            FilePart(partName, filename, contentType, file)
//          }(ec)
//      }
//
//      def uploadCustom = Action(parse.multipartFormData(handleFilePartAsFile)) { request =>
//        val fileOption = request.body.file("name").map {
//          case FilePart(key, filename, contentType, file) =>
//            file.toPath
//        }
//
//        Ok(s"File uploaded: $fileOption")
//      }
//      //#upload-file-customparser
//
//    }
//  }

