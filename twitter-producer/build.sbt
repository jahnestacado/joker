name := "twitter-producer"

version := "0.1"

enablePlugins(JavaAppPackaging)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.5",
  "com.typesafe" % "config" % "1.3.2",
  "org.apache.kafka" % "kafka-clients" % "2.1.0",
  "io.confluent" % "kafka-avro-serializer" % "5.0.1",
  "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.3"
)

scalaVersion := "2.12.8"

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
