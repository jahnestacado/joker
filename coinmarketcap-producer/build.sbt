name := "coinmarketcap-producer"

version := "0.1"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

// Define structure inside package/tarball
topLevelDirectory := None
name in Universal := name.value

enablePlugins(JavaAppPackaging, UniversalPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.apache.kafka" % "kafka-clients" % "2.1.0",
  "io.confluent" % "kafka-avro-serializer" % "5.0.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
)

scalaVersion := "2.12.8"

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
