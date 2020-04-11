import agency.{Agency, RunnerAgency}
import conifg.RabbitMqConfig
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

object AgencyApp extends App {

  val loadConfig: Either[ConfigReaderFailures, RabbitMqConfig] = {
    ConfigSource.default.load[RabbitMqConfig]
  }

  val agency = Agency("agency")

  loadConfig match {
    case Right(config) =>
      val runnerAgency = RunnerAgency(agency, config)
      runnerAgency.consume()
      runnerAgency.run()
  }
}
