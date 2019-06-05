package jahnestacado.postgresql.sink.kafka

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Materializer
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import akka.{Done, NotUsed}
import jahnestacado.postgresql.sink.Config
import jahnestacado.postgresql.sink.rdbms.{ConnectionPool, Persistor}

import scala.concurrent.{ExecutionContext, Future}

class ConsumerStream[T](connectionPool: ConnectionPool, config: Config, Persistor: Persistor[T])
                       (implicit executionContext: ExecutionContext, materializer: Materializer) {

  private val kafkaConsumerConfig: config.KafkaConsumer = config.kafkaConsumer
  private val parallelism: Int = kafkaConsumerConfig.numPartitions
  private val topic: String = Persistor.topic
  private val consumerSettings: ConsumerSettings[String, T] = kafkaConsumerConfig
    .getConsumerSettings[T](topic = topic)
  private val backoffStrategyConfig = kafkaConsumerConfig.backoffStrategy

  private val restartSource: Source[Done, NotUsed] = RestartSource.onFailuresWithBackoff(
    minBackoff = backoffStrategyConfig.min,
    maxBackoff = backoffStrategyConfig.max,
    randomFactor = backoffStrategyConfig.randomFactor
  ) { () =>
    Consumer
      .committablePartitionedSource(consumerSettings, Subscriptions.topics(topic))
      .flatMapMerge(parallelism, _._2)
  }
    .log("Received record", _.record.value())
    .mapAsync(parallelism) { msg =>
      val connection = connectionPool.get()
      Persistor
        .insert(connection.get, msg.record.value())
        .map(_ => msg.committableOffset)
    }
    .mapAsyncUnordered(parallelism)(_.commitScaladsl())
    .withAttributes(supervisionStrategy(resumingDecider))


  def run: Future[Done] = restartSource
    .runWith(Sink.ignore)

}
