package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.Config
import org.postgresql.jdbc3.Jdbc3PoolingDataSource

import scala.util.{Failure, Success, Try}

class ConnectionPool(config: Config) extends LazyLogging {
  private var connection: Option[Connection] = None

  private def initializeConnection(): Try[Connection] = {
    Try {
      val source = new Jdbc3PoolingDataSource()
      source.setServerName(config.postgresql.host)
      source.setPortNumber(config.postgresql.port)
      source.setDatabaseName(config.postgresql.database)
      source.setUser(config.postgresql.user)
      source.setPassword(config.postgresql.password)
      source.setMaxConnections(config.postgresql.maxConnections)
      source.getConnection()
    }
  }

  def get(): Option[Connection] = {
    if (connection.isEmpty) {
      initializeConnection() match {
        case Success(conn) =>
          logger.info(s"Established connection with postgres instance" +
            s" ${config.postgresql.host}:${config.postgresql.port}")
          connection = Some(conn)
        case Failure(ex) =>
          logger.error(ex.getMessage)
      }

      connection
    } else {
      Try(connection.get.getClientInfo()) match {
        case Success(_) =>
          connection
        case Failure(ex) =>
          logger.error(ex.getMessage)
          connection.get.close()
          connection = None
          connection
      }
    }
  }
}
