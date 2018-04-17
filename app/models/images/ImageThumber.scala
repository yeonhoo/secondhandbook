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
//    val imageBuf = ImageIO.read(image)
//    val height = imageBuf.getHeight
//    val width = imageBuf.getWidth
//

//
//    val imageName = imageName1(imageKey, thumbSize)
//    val (newWidth, newHeight) = ImageThumber.newSizesFor(thumbSize, width, height)
//    // I can call newSize method from a library
//    // and also call a substitute writeImage method
//    (writeImage(newWidth, newHeight, imageBuf, thumbSize, imageName), imageName)

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

    val name = file.getName

    (file, name)

  }
//  private def writeImage_v2 = ???
//
//  private def writeImage(width: Int, height: Int, imageBuf: BufferedImage,
//                         thumbSize: ThumbSize, imageName: String): File = {
//
//    val scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
//    val g = scaledImage.createGraphics
//    g.setComposite(AlphaComposite.Src)
//    g.drawImage(imageBuf, 0, 0, width, height, null);
//    g.dispose
//
//    val destFile = new File(image.getParentFile, imageName)
//    ImageIO.write(scaledImage, "png", destFile)
//    destFile
//  }
}

object ImageThumber {
  val sizes = List(SmallThumb, MediumThumb, LargeThumb, VeryLargeThumb)

  def newSizesFor_v2 = ???

  def newSizesFor(thumbSize: ThumbSize, originalWidth: Int, originalHeight: Int): (Int, Int) = {
    var newWidth: Double = originalWidth
    var newHeight: Double = originalHeight

    if (newWidth > thumbSize.width) {
      newWidth = thumbSize.width
      newHeight = originalHeight * newWidth / originalWidth
    }
    if (newHeight > thumbSize.height) {
      val oldHeight = newHeight
      newHeight = thumbSize.height
      newWidth = newHeight * newWidth / oldHeight
    }

    (newWidth.toInt, newHeight.toInt)
  }
}

sealed case class ThumbSize(width: Int, height: Int, suffix: String)
object SmallThumb extends ThumbSize(100, 100, "-small")
object MediumThumb extends ThumbSize(200, 200, "-medium")
object LargeThumb extends ThumbSize(300, 300, "-large")
object VeryLargeThumb extends ThumbSize(600, 600, "-verylarge")
object OriginalSize extends ThumbSize(0, 0, "")
