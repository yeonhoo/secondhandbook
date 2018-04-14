import models.images.{ImageThumber, Images, LargeThumb, SmallThumb}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder

class ImageThumberSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  "ImageThumber" should {
    "return the same width and height for sizes smaller than the thumb size" in {
      ImageThumber.newSizesFor(SmallThumb, 50, 60) mustBe (50, 60)
      ImageThumber.newSizesFor(SmallThumb, 70, 60) mustBe (70, 60)
    }

    "return correct new sizes when the real width is twice the thumb width" in {
      ImageThumber.newSizesFor(SmallThumb, 200, 100) mustBe (100, 50)
      }

    "return correct new sizes when both dimensions are bigger than the thumb's and width > height" in {
      ImageThumber.newSizesFor(SmallThumb, 500, 375) mustBe (100, 75)
    }

    "return correct new sizes when both dimensions are bigger than the thumb's and height > width" in {
      ImageThumber.newSizesFor(SmallThumb, 375, 500) mustBe(75, 100)
    }
  }
}

//package images
//
//import models.images.{ImageThumber, SmallThumb}
//import org.specs2.mutable.Specification
//
//class ImageThumberSpec extends Specification {
//  "the ImageThumber" should {
//    "return the same width and height for sizes smaller than the thumb size" in {
//      ImageThumber.newSizesFor(SmallThumb, 50, 60) must_== (50, 60)
//      ImageThumber.newSizesFor(SmallThumb, 70, 60) must_== (70, 60)
//    }
//
//    "return correct new sizes when the real width is twice the thumb width" in {
//      ImageThumber.newSizesFor(SmallThumb, 200, 100) must_== (100, 50)
//    }
//
//    "return correct new sizes when both dimensions are bigger than the thumb's and width > height" in {
//      ImageThumber.newSizesFor(SmallThumb, 500, 375) must_== (100, 75)
//    }
//
//    "return correct new sizes when both dimensions are bigger than the thumb's and height > width" in {
//      ImageThumber.newSizesFor(SmallThumb, 375, 500) must_== (75, 100)
//    }
//  }
//}
