package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import akka.Done
import com.jahnestacado.model.Tweet

import scala.concurrent.{ExecutionContext, Future}


object TweetPersistor extends Persistor[Tweet] {
  private val tableName = "Tweets"

  //  created_at timestamptz


  def createTable(connection: Connection) = {
    val preparedStatement = connection.prepareStatement(
      s"""CREATE TABLE IF NOT EXISTS ${tableName} (
      insertion_time timestamptz DEFAULT current_timestamp,
      created_at text,
      id bigint,
      text text,
      in_reply_to_status_id bigint,
      in_reply_to_user_id bigint,
      is_quote_status boolean,
      retweet_count bigint,
      favorite_count bigint,
      hashtags text[],
      users_mentions_by bigint[],
      favorited boolean,
      retweeted boolean,
      filter_level text,
      lang text,
      user_name text,
      user_id bigint,
      user_location text,
      user_description text,
      user_translator_type text,
      user_protected boolean,
      user_verified boolean,
      user_followers_count integer,
      user_friends_count integer,
      user_listed_count integer,
      user_statuses_count integer,
      user_favourites_count integer,
      user_created_at text,
      user_lang text,
      user_following boolean,
      user_follow_request_sent boolean,
      user_notifications boolean
    );""")
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }

  def insert(connection: Connection, tweet: Tweet)(implicit executionContext: ExecutionContext): Future[Done] = Future {
    val statement = connection.prepareStatement(
      s"""INSERT INTO ${tableName} (
            created_at,
            id,
            text,
            in_reply_to_status_id,
            in_reply_to_user_id,
            is_quote_status,
            retweet_count,
            favorite_count,
            hashtags,
            users_mentions_by,
            favorited,
            retweeted,
            filter_level,
            lang,
            user_name,
            user_id,
            user_location,
            user_description,
            user_translator_type,
            user_protected,
            user_verified,
            user_followers_count,
            user_friends_count,
            user_listed_count,
            user_statuses_count,
            user_favourites_count,
            user_created_at,
            user_lang,
            user_following,
            user_follow_request_sent,
            user_notifications
        ) VALUES (${"?, " * 30} ?);""")

    statement.setString(1, tweet.created_at)
    statement.setLong(2, tweet.id)
    statement.setString(3, tweet.text)
    statement.setLong(4, tweet.in_reply_to_status_id.getOrElse(0))
    statement.setLong(5, tweet.in_reply_to_user_id.getOrElse(0))
    statement.setBoolean(6, tweet.is_quote_status)
    statement.setLong(7, tweet.retweet_count)
    statement.setLong(8, tweet.favorite_count)
    statement.setArray(9, connection.createArrayOf("text", tweet.hashtags.get.toArray))
    statement.setArray(10, connection.createArrayOf("bigint", tweet.users_mentions_by.get.toArray[Any].asInstanceOf[Array[AnyRef]]))
    statement.setBoolean(11, tweet.favorited)
    statement.setBoolean(12, tweet.retweeted)
    statement.setString(13, tweet.filter_level.getOrElse(""))
    statement.setString(14, tweet.lang.getOrElse(""))

    tweet.user match {
      case Some(user) => {
        statement.setString(15, user.name)
        statement.setLong(16, user.id)
        statement.setString(17, user.location.getOrElse(""))
        statement.setString(18, user.description.getOrElse(""))
        statement.setString(19, user.translator_type.getOrElse(""))
        statement.setBoolean(20, user.`protected`)
        statement.setBoolean(21, user.verified)
        statement.setInt(22, user.followers_count)
        statement.setInt(23, user.friends_count)
        statement.setInt(24, user.listed_count)
        statement.setInt(25, user.statuses_count)
        statement.setInt(26, user.favourites_count)
        statement.setString(27, user.created_at)
        statement.setString(28, user.lang)
        statement.setBoolean(29, user.following)
        statement.setBoolean(30, user.follow_request_sent)
        statement.setBoolean(31, user.notifications)
      }
      case None => print("User is None")
    }

    statement.executeUpdate()
    statement.close()
    Done
  }

}
