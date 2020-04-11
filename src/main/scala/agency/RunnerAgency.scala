package agency


import com.rabbitmq.client.{AMQP, BuiltinExchangeType, DefaultConsumer, Envelope}
import conifg.RabbitMqConfig
import conveyor.Conveyor
import conveyor.Conveyor.{PlaceSatelliteOrbit, TransitLoad, TransitPeople}
import message.JsonMessage
import message.TypeMessage.{Approved, Commission}
import runner.RunnerRabbitMq

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class RunnerAgency(agency: Agency, config: RabbitMqConfig) extends RunnerRabbitMq {

  channel.exchangeDeclare(config.exchange, BuiltinExchangeType.TOPIC)

  val queueAgency: String = s"Agency.${agency.name}"
  val routingKeyAgency = s"agency.${agency.name}"
  bindQueue(s"Agency.${agency.name}", routingKeyAgency)

  channel.basicQos(1)

  def run(): Unit = {
    Future {
      @annotation.tailrec
      def produce(): Unit = {
        System.out.println("Enter type of service: ")
        val readLine = scala.io.StdIn.readLine
        val service = extractService(readLine)

        val ticket = agency.generateTicket(service)
        val key = Conveyor.getKeyFromService(service)

        val jsonMessage = JsonMessage(Commission, Some(ticket))
        val message = JsonMessage.jsonMessageEncoder.apply(jsonMessage).noSpaces

        channel.basicPublish(config.exchange, key, null, message.getBytes("UTF-8"))
        produce()
      }

      produce()
    }
  }

  private def extractService(value: String): Conveyor.Service = {
    value match {
      case "TransitLoad" => TransitLoad
      case "TransitPeople" => TransitPeople
      case "PlaceSatelliteOrbit" => PlaceSatelliteOrbit
    }
  }

  val consumer= new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
      val jsonMessage = decodeMessage(body)
      jsonMessage.typeMessage match {
        case Commission => scribe.error("Invalid type of message for Agency")
        case Approved => consumeApproved(jsonMessage.ticket)
      }

      basicAck(envelope)
    }
  }

  def consumeApproved(ticket: Option[Ticket]) = {
    ticket match {
      case None => scribe.error("Empty ticket for approval")
      case Some(t) => {
        scribe.info(s"""Agency received approved ticket: ${t.getMessage()}""")
      }
    }
  }

  def consume() = {
    channel.basicConsume(queueAgency, false, consumer)
  }


}
