package db

import slick.jdbc.PostgresProfile.api._

object DatabaseConnection {
  val db = Database.forConfig("mydb")
}
