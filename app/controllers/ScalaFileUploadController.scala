package controllers

import java.io.File
import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Paths, Files => JFiles}
import javax.inject.Inject

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.ExecutionContext

class ScalaFileUploadController @Inject()(controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {

  //#upload-file-directly-action
  def upload = Action(parse.temporaryFile) { request =>
    request.body.moveTo(Paths.get("/tmp/picture/uploaded"), replace = true)
    Ok("File uploaded")
  }
  //#upload-file-directly-action

  def index = Action { request =>
    Ok("Upload failed")
  }

  //#upload-file-customparser
  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val perms = java.util.EnumSet.of(OWNER_READ, OWNER_WRITE)
      val attr = PosixFilePermissions.asFileAttribute(perms)
      val path = JFiles.createTempFile("multipartBody", "tempFile", attr)
      val file = path.toFile
      val fileSink = FileIO.toPath(path)
      val accumulator = Accumulator(fileSink)
      accumulator.map { case IOResult(count, status) =>
        FilePart(partName, filename, contentType, file)
      }(ec)
  }

  def uploadCustom = Action(parse.multipartFormData(handleFilePartAsFile)) { request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file) =>
        file.toPath
    }

    Ok(s"File uploaded: $fileOption")
  }
  //#upload-file-customparser

}