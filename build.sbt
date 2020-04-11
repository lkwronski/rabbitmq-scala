name := "rabbitmq-scala"

version := "0.1"

scalaVersion := "2.13.1"
val circeVersion = "0.13.0"

libraryDependencies += "com.rabbitmq" % "amqp-client" % "5.8.0"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.12.3"
libraryDependencies += "com.outr" %% "scribe" % "2.7.12"
libraryDependencies += "com.beachape" %% "enumeratum-macros" % "1.5.10"
libraryDependencies += "com.beachape" %% "enumeratum-circe" % "1.5.23"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
