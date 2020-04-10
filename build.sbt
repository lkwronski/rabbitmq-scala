name := "rabbitmq-scala"

version := "0.1"

scalaVersion := "2.13.1"


libraryDependencies += "com.rabbitmq" % "amqp-client" % "5.8.0"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.12.3"
libraryDependencies += "com.outr" %% "scribe" % "2.7.12"