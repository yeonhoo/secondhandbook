package models.images

import akka.actor._
import akka.routing._
import java.io.File

import models.aws.{S3Sender, Sender}

import scala.util.Random
import javax.inject._

import akka.actor.ActorSystem
import play.api.Configuration


// really it does need to be Singleton? I think it does because it takes actorSystem as argument
// it there are more than one Images object, ther would be more than one actorSystem? i'm not sure
@Singleton
class Images @Inject()(actorSystem: ActorSystem, config: Configuration) {

  val thumberRouter =
    actorSystem.actorOf(Props[ImageThumberActor].withRouter(SmallestMailboxPool(2)), "thumber-router")

  val s3Sender = new S3Sender(
    config.get[String]("aws.accessKey"),
    config.get[String]("aws.secretKey"),
    config.get[String]("aws.s3.bucket"))

  val s3SenderRouter = actorSystem.actorOf(Props(new S3SenderActor(s3Sender))
      .withRouter(SmallestMailboxPool(4)), "s3-sender-router")

  /**
   * Generates a key for the image and returns it immediatelly, while sending the
   * image to be processed asynchronously with akka.
   */
  def processImage(image: File): String = {
    val validChars = "abcdefghijklmnopqwxyv_"
    val imageKey = (1 to 20).foldLeft("")((t, a) => t + validChars(Random.nextInt(validChars.length)))
    thumberRouter ! GenThumb(image, imageKey)

    imageKey
  }

  def generateUrl(imageKey: String, thumbSize: ThumbSize): String = {
    "%s/%s/%s".format(
      config.get[String]("aws.s3.server"),
      config.get[String]("aws.s3.bucket"),
      imageName(imageKey, thumbSize)
    )
  }

  def imageName(imageKey: String, thumbSize: ThumbSize): String = imageKey + thumbSize.suffix + ".jpg"
}

class ImageThumberActor extends Actor with ActorLogging {
  def receive = {
    case GenThumb(image, imageKey) =>
      log.info("about to generate thumbs for key {}", imageKey)

      val images = new ImageThumber(image, imageKey).generateThumbs
      val s3SenderRouter = context.system.actorSelection("akka://application/user/s3-sender-router")

      s3SenderRouter ! SendToS3(image, imageKey + ".jpg")
      images foreach { imageTuple =>
        val (imageFile, imageName) = imageTuple
        s3SenderRouter ! SendToS3(imageFile, imageName)
      }
  }
}

// constructor arg type is Sender, for the sake of unit testing it wouldn't be the exact maching type (S3Sender2)
class S3SenderActor(s3Sender: Sender) extends Actor with ActorLogging {
  def receive = {
    case SendToS3(image, imageKey) =>
      log.info("about to send {} to s3", imageKey)
      s3Sender.send(image, imageKey)
  }
}

case class GenThumb(image: File, imageKey: String)
case class SendToS3(image: File, imageName: String)
