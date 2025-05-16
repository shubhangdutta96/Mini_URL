package tables

import actors.urlActor._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import java.time.ZonedDateTime

class URLMappingTable(tag: Tag) extends Table[URLMapping](tag, "urls") {
  import URLMappingTable.zonedDateTimeColumnType // ✅ bring implicit into scope

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def originalUrl = column[String]("original_url")

  def shortCode = column[String]("short_code")

  def createdAt = column[ZonedDateTime]("created_at")

  def expiry = column[Option[ZonedDateTime]]("expiry")

  def clickCount = column[Int]("click_count")

  def * = (id.?, originalUrl, shortCode, createdAt, expiry, clickCount) <> (URLMapping.tupled, URLMapping.unapply)
}

object URLMappingTable {
  val table = TableQuery[URLMappingTable]

  // ✅ Put the implicit here — inside the companion object
  implicit val zonedDateTimeColumnType: JdbcType[ZonedDateTime] with BaseTypedType[ZonedDateTime] =
    MappedColumnType.base[ZonedDateTime, Timestamp](
      zdt => Timestamp.from(zdt.toInstant),
      ts => ZonedDateTime.ofInstant(ts.toInstant, java.time.ZoneOffset.UTC)
    )
}
