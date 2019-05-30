name := "coinmarketcap-producer"

version := "0.1"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

// Define structure inside package/tarball
topLevelDirectory := None
name in Universal := name.value

enablePlugins(JavaAppPackaging, UniversalPlugin)

val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "io.spray" %% "spray-json" % "1.3.5",
  "io.confluent" % "kafka-avro-serializer" % "5.2.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
)

scalaVersion := "2.12.8"

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
