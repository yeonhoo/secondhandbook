
import java.io._

import models.images.{ImageThumber, Images, LargeThumb, ThumbSize}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage
import java.net.URL
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

import com.sksamuel.scrimage.Image
import models.aws.S3Sender

class ImagesSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  def images = app.injector.instanceOf(classOf[Images])

  // aqui eu to startando uma aplicacao falsa, enquanto no teste de imageThumberSpec apenas instancio uma aplicacao
  override def fakeApplication() = new GuiceApplicationBuilder().configure(
    Map("aws.s3.bucket" -> "testando.yun")).build()

  //val image = Image.fromResource("/home/yun/Desktop/20161020_163045.jpg")

  abstract class BehaviorDecorator extends InputStream {
    def available: Int
  }

  class ImageFileStream(inputStream: InputStream) extends BehaviorDecorator {
    override def read(): Int = inputStream.read()
    override def available(): Int = 1
  }

  "Images" should {

    //TODO fix
//    "send to s3" in {
//
//      val img: File  = new File("/home/yun/Desktop/test-verylarge.png")
//      val imgKey = "testKey.png"
//      new S3Sender(img, imgKey).send
//
//    }

    "generateThumb" in {

      // load from local file
      val inputFromFile:InputStream  = new BufferedInputStream(
        new FileInputStream("/home/yun/Desktop/x9788934993766.jpg"));

      //local file without decorator
      val loadedImg = Image.fromStream(inputFromFile).scaleToHeight(100)
                            .output("/home/yun/Desktop/resize-100-100")

      import models.images.ImageThumber

      val folderInput: File  = new File("/home/yun/Desktop/x4801160504225.jpg")

      println(" what is the name => " +
      Image.fromFile(folderInput).scaleToHeight(100)
        .output("/home/yun/Desktop/resize3-100-100").toFile.getName
      )

      val images = new ImageThumber(folderInput, "blabla").generateThumbs

      val names = images.map( _._1.getName)
      names.foreach(println(_))

      names.size mustBe 4

//      val names = result.map( el => el._1.getName )
//      names mustBe a [List[_]]
//      names.size mustBe 4
//      names must contain ("test-medium.png")
//      names must contain theSameElementsAs List("test-small.png", "test-medium.png", "test-large.png", "test-verylarge.png")
//


      //
//      val folderInput: File  = new File("/tmp/duke.png");
//      val folderImage: BufferedImage  = ImageIO.read(folderInput);
//
//      // read image from class-path
//      val classPathInput: File  = new File(ReadImageExample.class.getResource("duke.png").getFile());
//      val classpathImage: BufferedImage  = ImageIO.read(classPathInput);
//
//      // read image from inputstream
//      val isInput: InputStream = new FileInputStream("/tmp/duke.png");
//      val inputStreamImage: BufferedImage  = ImageIO.read(isInput);


      //image.scaleTo(486, 324) shouldBe Image.fromResource("/bird_486_324.png")
    }

    "load from local file" in {
      //val image = Image.fromResource("/home/yun/Desktop/20161020_163045.jpg")


      // load from local file
      val inputFromFile:InputStream  = new BufferedInputStream(
        new FileInputStream("/home/yun/Desktop/x9788934993766.jpg"));

      //local file without decorator
      Image.fromStream(inputFromFile).scale(0.5, Bicubic).output("/home/yun/Desktop/outputfile")

      // local file with decorator
      // doesn't need to use decorator since its directly loaded from local file
      //val jpgInput = new ImageFileStream(inputFromFile)
      //Image.fromStream(jpgInput).scale(0.5, Bicubic).output("/home/yun/Desktop/outputFile")

      1 mustBe 1
    }


    "load from URL" in {
      // load from URL
      val imageUrl = "https://s3-sa-east-1.amazonaws.com/testando.yun/x9788934993766.jpg"
      val inputFromURL: InputStream = new URL(imageUrl).openStream()

      // web file with decorator
      val jpgFromWeb = new ImageFileStream(inputFromURL)
      Image.fromStream(jpgFromWeb).scale(0.5, Bicubic).output("/home/yun/Desktop/outputWeb")

      // web file without decorator
      // gives me an exception // requirement failed
      //Image.fromStream(inputFromURL).scale(0.5, Bicubic).output("/home/yun/Desktop/outputWeb")

    }

    "generate the thumbs of size" in {

      import com.sksamuel.scrimage.Image

      try {
        //val image: BufferedImage  = ImageIO.read(new File("/home/yun/Desktop/x9788934993766.jpg"))

        val file = new java.io.File("/home/yun/Desktop/x9788934993766.jpg")

//        val is: InputStream = new BufferedInputStream(
//          new FileInputStream("/home/yun/Desktop/x9788934993766.jpg"));

        val image = Image.fromResource("/com/sksamuel/scrimage/bird.jpg")
        Image.fromResource("/home/yun/Desktop/20161020_163045.jpg")

        //val outputFile = Image.fromStream(is)


//        val outputFile = Image.fromFile(file)
//          .scale(0.5, Bicubic)//.output("/home/yun/Desktop/20161020_163045.jpg")
//
//        println(outputFile.toString + " ?!?! ta funfando?")


        //val file = new java.io.File("/home/yun/Desktop/x10.jpg")

        file.exists() mustBe true
//        outputFile.getFileName.toString.contains("new") mustBe true

      } catch {
        case e: java.io.IOException => println("IO exceptionsosas!?!?!!?")
        case ia: IllegalArgumentException => println("ilegal argument???")
      }


      //Image.fromFile("/home/yun/Desktop/x9788934993766.jpg")
    }

    "generate the correct image name" in {
      images.imageName("asdf", LargeThumb) mustBe "asdf-large.png"
    }

    "generate the correct AWS s3 URL" in {
      val bucket = app.configuration.get[String]("aws.s3.bucket")
      val imageKey = "my-random-imageKey"

      //"https://s3[-sa-east-1].amazonaws.com/testa..." was not equal to
      // "https://s3[].amazonaws.com/testa..."

      //Expected :"https://s3[.]-sa-east-1.amazonaws..."
      //Actual   :"https://s3[]-sa-east-1.amazonaws..."


      images.generateUrl(imageKey, LargeThumb) mustBe "https://s3-sa-east-1.amazonaws.com/%s/%s-large.png".format(bucket, imageKey)
    }
  }
}
//
//class ExampleSpec extends PlaySpec with GuiceOneAppPerSuite {
//
//  // Override fakeApplication if you need a Application with other than
//  // default parameters.
//  override def fakeApplication() = new GuiceApplicationBuilder().configure(
//    Map("aws.s3.bucket" -> "testando.yun")).build()
//
//  "The GuiceOneAppPerSuite trait" must {
//    "provide an Application" in {
//      app.configuration.getOptional[String]("ehcacheplugin") mustBe Some("disabled")
//    }
//    "start the Application" in {
//      Play.maybeApplication mustBe Some(app)
//    }
//  }
//}