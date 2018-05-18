package models.domain

import java.time.LocalDate

case class Comment(id: Option[Long] = None,
                   content: String,
                   created: Option[LocalDate] = None,
                   userId: Long,
                   status: Long,
                   bookId: Long)
