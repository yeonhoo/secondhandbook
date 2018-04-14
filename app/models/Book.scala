package models

case class Book(id: Option[Long] = None,
                name: String,
                price: Long,
                author: Option[String],
                description: Option[String],
                imgKey: Option[String],
                reserved: Option[Boolean],
                publisherId: Option[Long],
                userId: Option[Long])

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

