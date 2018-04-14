package models.images

import akka.actor._
import akka.routing._
import java.io.File
import models.aws.S3Sender
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.util.Random
import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}


// really it does need to be Singleton? I think it does because it takes actorSystem as argument
// it there are more than one Images object, ther would be more than one actorSystem? i'm not sure
@Singleton
class Images @Inject()(actorSystem: ActorSystem) {
  val thumberRouter =
    actorSystem.actorOf(Props[ImageThumberActor].withRouter(SmallestMailboxPool(2)), "thumber-router")
  val s3SenderRouter =
    actorSystem.actorOf(Props[S3SenderActor].withRouter(SmallestMailboxPool(4)), "s3-sender-router")

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

    "https://s3-sa-east-1.amazonaws.com/%s/%s".format(
      current.configuration.getString("aws.s3.bucket").get,
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

class S3SenderActor extends Actor with ActorLogging {
  def receive = {
    case SendToS3(image, imageKey) =>
      log.info("about to send {} to s3", imageKey)
      new S3Sender(image, imageKey).send
  }
}

case class GenThumb(image: File, imageKey: String)
case class SendToS3(image: File, imageName: String)
