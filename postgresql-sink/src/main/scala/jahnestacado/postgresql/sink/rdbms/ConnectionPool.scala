package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.Config
import org.postgresql.jdbc3.Jdbc3PoolingDataSource

class ConnectionPool(config: Config) extends LazyLogging {
  private var connection: Option[Connection] = None

  private def initializeConnection(): Option[Connection] = {
    val source = new Jdbc3PoolingDataSource()
    source.setServerName(config.postgresql.host)
    source.setPortNumber(config.postgresql.port)
    source.setDatabaseName(config.postgresql.database)
    source.setUser(config.postgresql.user)
    source.setPassword(config.postgresql.password)
    source.setMaxConnections(config.postgresql.maxConnections)
    logger.info(s"Established connection with postgres instance ${config.postgresql.host}:${config.postgresql.port}")
    Some(source.getConnection())
  }

  def get(): Option[Connection] = {
    if (connection.isEmpty) {
      connection = initializeConnection()
      connection
    } else {
      try {
        connection.get.getClientInfo()
        connection
      } catch {
        case ex: Exception =>
          logger.error(ex.getMessage())
          connection.get.close()
          connection = None
          connection
      }
    }
  }
}
