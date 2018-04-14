package models.parsers

import anorm.SqlParser.str
import anorm.{Column, MetaDataItem, TypeDoesNotMatch, ~}
import org.postgresql.util.PGobject

object UserParsers {
//
//  val parseUserId = str("user_id").map(UserId.apply)
//  val parseEmail = str("email")(citextToString).map(UserEmail.apply)
//
//  val parseUser = (parseUserId ~ parseEmail).map {
//    case userId ~ email => Userx.apply(userId, email)
//  }
//
//
//  private def citextToString: Column[String] = Column.nonNull { case (value, meta) =>
//    val MetaDataItem(qualified, _, clazz) = meta
//    value match {
//      case str: String => Right(str)
//      case obj: PGobject if "citext" equalsIgnoreCase obj.getType => Right(obj.getValue)
//      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to String for column $qualified, class = $clazz"))
//    }
//  }
}