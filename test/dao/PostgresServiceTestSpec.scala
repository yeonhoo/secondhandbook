
import commons.PostgresDataHandlerSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite


class PostgresServiceTestSpec extends PostgresDataHandlerSpec {

  "all containers" should {
    "be ready at the same time" in {
      isContainerReady(postgresContainer).futureValue mustEqual true
      //dockerContainers.map(_.image).foreach(println)
      //dockerContainers.forall(_.isReady().futureValue) shouldBe true
    }
  }
}
