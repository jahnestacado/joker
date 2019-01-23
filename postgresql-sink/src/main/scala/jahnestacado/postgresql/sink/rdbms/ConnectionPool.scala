package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import jahnestacado.postgresql.sink.Config
import org.postgresql.jdbc3.Jdbc3PoolingDataSource

class ConnectionPool(config: Config) {
  private var connection: Connection = null

  private def initializeConnection(): Connection = {
    val source = new Jdbc3PoolingDataSource()
    source.setServerName(config.postgresql.host)
    source.setPortNumber(config.postgresql.port)
    source.setDatabaseName(config.postgresql.database)
    source.setUser(config.postgresql.user)
    source.setPassword(config.postgresql.password)
    source.setMaxConnections(config.postgresql.maxConnections)
    source.getConnection()
  }

  def get(): Connection = {
    if (connection == null) {
      connection = initializeConnection()
      connection
    } else {
      try {
        connection.getClientInfo()
        connection
      } catch {
        case e: Exception =>
          println(e.getMessage)
          connection.close()
          connection = null
          connection
      }
    }
  }
}
