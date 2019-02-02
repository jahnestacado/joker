package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

trait Persistor[T] {
  val topic: String

  def createTable(connection: Connection)

  def insert(connection: Connection, record: T)(implicit executionContext: ExecutionContext): Future[Done]
}
