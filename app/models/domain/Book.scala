package models.domain

case class Book(id: Option[Long] = None,
                title: String,
                author: String,
                description: String,
                price: Long,
                imgKeys: Option[String],
                status: Int,
                upCount: Int,
                downCount: Int,
                userId: Long,
                publisherId: Option[Long])
