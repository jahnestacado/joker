name := "postgresql-sink"

version := "0.1"

resolvers ++= Seq(
  Classpaths.typesafeReleases,
  "confluent" at "http://packages.confluent.io/maven/"
)


// Define structure inside package/tarball
topLevelDirectory := None
name in Universal := name.value

enablePlugins(JavaAppPackaging, UniversalPlugin)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "2.1.0",
  "io.confluent" % "kafka-avro-serializer" % "5.0.1",
  "org.postgresql" % "postgresql" % "42.2.5",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0-RC1"
)

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue

scalaVersion := "2.12.8"

