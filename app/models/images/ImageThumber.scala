package models.images

import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO

import com.sksamuel.scrimage.Image

class ImageThumber(image: File, imageKey: String) {
  def generateThumbs(): List[(File, String)] = ImageThumber.sizes map generateThumb

  def generateThumb(thumbSize: ThumbSize): (File, String) = {

// maybe need to refactor here there is imageName function in Images class
  def imageName1(imageKey: String, thumbSize: ThumbSize): String = imageKey + thumbSize.suffix + ".jpg"


    // acho que da pra usar essa funcao pra criar o arquivo temporario no dir default
// val newFile = File.createTempFile("temp-uploaded-", "test")
//  newFile.path

    //TODO
    // Create volume directories explicitly so that they are created with correct owner
    //Files.createDirectories(Paths.get("/temporary/mydir/"));

    //TODO refactor
    val file = Image.fromFile(image).scaleToHeight(thumbSize.height)
    .output(s"/tmp/${imageName1(imageKey, thumbSize)}").toFile

    (file, file.getName)
  }
}

object ImageThumber {
  val sizes = List(SmallThumb, MediumThumb, LargeThumb, VeryLargeThumb)
}

sealed case class ThumbSize(width: Int, height: Int, suffix: String)
object SmallThumb extends ThumbSize(100, 100, "-small")
object MediumThumb extends ThumbSize(200, 200, "-medium")
object LargeThumb extends ThumbSize(300, 300, "-large")
object VeryLargeThumb extends ThumbSize(600, 600, "-verylarge")
object OriginalSize extends ThumbSize(0, 0, "")
