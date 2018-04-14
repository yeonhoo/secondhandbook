package models.dao

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.Publisher
import play.api.db.Database

import scala.concurrent.Future


@javax.inject.Singleton
class PublisherDAO @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {

  //private val db = dbapi.database("default")

  /**
    * Parse a Publisher from a ResultSet
    */
  private[models] val simple = { // type : RowParser[Publisher]
    get[Option[Long]]("publisher.id") ~
      get[String]("publisher.name") map {
      case id~name => Publisher(id, name)
    }
  }

  /**
    * Construct the Map[String,String] needed to fill a select options set.
    */
  def options: Future[Seq[(String,String)]] = Future(db.withConnection { implicit connection =>
    SQL("select * from publisher order by name").as(simple *). //RowParser[Publisher].* => ResultSetParser[List[Publisher]]
      // "as" transform ResultSetParser[scala.List[A]] to List[A]
      // I think " List[A] is mapped as Seq[(String, String)] cuz "A" is somehow (String, String)
      foldLeft[Seq[(String, String)]](Nil) { (cs, c) =>
      c.id.fold(cs) { id => cs :+ (id.toString -> c.name) }
      // List[Publisher] to Seq[(id,name)]
    }
  })(ec)



}
