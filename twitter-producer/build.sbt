name := "twitter-producer"

version := "0.1"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

// Define structure inside package/tarball
topLevelDirectory := None
name in Universal := name.value

enablePlugins(JavaAppPackaging, UniversalPlugin)

resolvers += "confluent" at "http://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.5",
  "com.typesafe" % "config" % "1.3.2",
  "org.apache.kafka" % "kafka-clients" % "2.1.0",
  "io.confluent" % "kafka-avro-serializer" % "5.0.1",
  "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

scalaVersion := "2.12.8"

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
