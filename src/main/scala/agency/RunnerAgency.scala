package agency


import com.rabbitmq.client.{BuiltinExchangeType, Channel, Connection, ConnectionFactory}
import conifg.RabbitMqConfig
import conveyor.Conveyor
import conveyor.Conveyor.{PlaceSatelliteOrbit, TransitLoad, TransitPeople}

case class RunnerAgency(agency: Agency, config: RabbitMqConfig) {

  val factory = new ConnectionFactory
  factory.setHost(config.host)
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel

  channel.exchangeDeclare(config.exchange, BuiltinExchangeType.TOPIC)

  def run(): Unit = {

    @annotation.tailrec
    def produce(): Unit = {
      System.out.println("Enter type of service: ")
      val readLine = scala.io.StdIn.readLine
      val service = extractService(readLine)

      val ticket = agency.generateTicket(service)
      val key = Conveyor.getKeyFromService(service)
      val message = ticket.getMessage()

      channel.basicPublish(config.exchange, key, null, message.getBytes("UTF-8"))
      produce()
    }

    produce()
  }

  private def extractService(value: String): Conveyor.Service = {
    value match {
      case "TransitLoad" => TransitLoad
      case "TransitPeople" => TransitPeople
      case "PlaceSatelliteOrbit" => PlaceSatelliteOrbit
    }
  }

}
