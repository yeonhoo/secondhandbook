package commons

import com.typesafe.config.ConfigFactory
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

//GuiceOneAppPerSuite inicializa a Application, which creates automatically the Application through GuiceApplicationBuilder
//So if I don't want to initialize the Application, I shouldn't extend the GuiceOneAppPerSuite
class CanLoadConfigSpec extends PlaySpec {


  val customConsfiguration: Configuration = {
    // Load a completely different test configuration file for testing. Put config file in conf folder
    val testConfig = ConfigFactory.load("dev.conf")
    Configuration(testConfig)
  }

  // create a app with that Configuration
  val application = new GuiceApplicationBuilder()
    .loadConfig(customConsfiguration)
    .configure(Configuration("a" -> 1))
    .build()

  // get the Configuration from the application
  val config: Configuration  = application.injector.instanceOf(classOf[Configuration])


  "database name" should {
    "load the right key" in {
      config.get[String]("play.db.default") mustBe "dev"
      customConsfiguration.get[String]("play.db.default") mustBe "dev"
      //customConsfiguration.get[String]("a") mustBe 1
    }
  }

}
