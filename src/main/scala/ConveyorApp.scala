import conifg.RabbitMqConfig
import conveyor.{Conveyor, RunnerConveyor}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

object ConveyorApp extends App {

  val loadConfig: Either[ConfigReaderFailures, RabbitMqConfig] = {
    ConfigSource.default.load[RabbitMqConfig]
  }

  loadConfig match {
    case Right(config) => {
      RunnerConveyor(Conveyor.TransitLoad, config).consume()
      RunnerConveyor(Conveyor.PlaceSatelliteOrbit, config).consume()
    }
  }
}
