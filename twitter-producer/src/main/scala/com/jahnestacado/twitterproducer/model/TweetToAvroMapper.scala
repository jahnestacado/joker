package com.jahnestacado.twitterproducer.model

import com.danielasfregola.twitter4s.entities.{Entities, Tweet => TweetSrc, User => UserSrc}
import com.jahnestacado.model.{Tweet, User}

object TweetToAvroMapper {
  def mapTweet(_tweet: TweetSrc): Tweet = new Tweet(
    created_at = _tweet.created_at.toString,
    id = _tweet.id,
    text = _tweet.text,
    in_reply_to_user_id = _tweet.in_reply_to_user_id,
    in_reply_to_status_id = _tweet.in_reply_to_status_id,
    is_quote_status = _tweet.is_quote_status,
    retweet_count = _tweet.retweet_count,
    favorite_count = _tweet.favorite_count,
    hashtags = _tweet.entities match {
      case Some(e: Entities) => Some(e.hashtags.map(_.text))
      case None => Option.empty[Seq[String]]
    },
    users_mentions_by = _tweet.entities match {
      case Some(e: Entities) => Some(e.user_mentions.map(_.id))
      case None => Option.empty[Seq[Long]]
    },
    favorited = _tweet.favorited,
    retweeted = _tweet.retweeted,
    filter_level = _tweet.filter_level,
    lang = _tweet.lang,
    user = mapUser(_tweet.user),
  )


  def mapUser(_user: Option[UserSrc]): Option[User] = _user match {
    case Some(u: UserSrc) => {
      Some(new User(
        name = u.name,
        id = u.id,
        location = u.location,
        description = u.description,
        translator_type = u.translator_type,
        `protected` = u.`protected`,
        verified = u.verified,
        followers_count = u.followers_count,
        friends_count = u.friends_count,
        listed_count = u.listed_count,
        statuses_count = u.statuses_count,
        favourites_count = u.favourites_count,
        created_at = u.created_at.toString,
        lang = if(u.lang == null) None else Some(u.lang),
        following = u.following,
        follow_request_sent = u.follow_request_sent,
        notifications = u.notifications
      ))
    }

    case None => None
  }


}
