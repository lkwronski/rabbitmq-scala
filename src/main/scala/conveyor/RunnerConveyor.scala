package conveyor

import agency.Ticket
import com.rabbitmq.client._
import conifg.RabbitMqConfig
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import message.JsonMessage
import message.TypeMessage.{Approved, Commission}
import runner.RunnerRabbitMq

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class RunnerConveyor(service: Conveyor.Service, config: RabbitMqConfig) extends RunnerRabbitMq {

  scribe.info(s"""Init runner Conveyor with service: $service""")

  channel.exchangeDeclare(config.exchange, BuiltinExchangeType.TOPIC)

  val queueConveyor: String = service.toString.toLowerCase
  val routingKeyConveyor = s"${service.toString.toLowerCase}.conveyor"
  bindQueue(queueConveyor, routingKeyConveyor)
//  channel.basicQos(1)

  scribe.info(s"""Bind queue with key: $routingKeyConveyor""")

  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
      val jsonMessage = decodeMessage(body)
      jsonMessage.typeMessage match {
        case Approved => scribe.error("Invalid type of message for Conveyor")
        case Commission => consumeCommission(jsonMessage.ticket)
      }
      val deliveryTag = envelope.getDeliveryTag
      channel.basicAck(deliveryTag, false)
    }
  }

  private def consumeCommission(ticket: Option[Ticket]) = {
    ticket match {
      case None => scribe.error("Empty ticket for commission")
      case Some(t) => {
        val nameAgency = t.nameAgency
        scribe.info(s"""Conveyor receive ticket: service: $service received: ${t.getMessage()}""")
        val jsonMessageResponse = JsonMessage(Approved, ticket)
        val response = encodeMessage(jsonMessageResponse)
        channel.basicPublish(config.exchange, s"agency.$nameAgency", null, response.getBytes("UTF-8"))
      }
    }
  }

  def consume(): Unit ={
      channel.basicConsume(queueConveyor, false, consumer)
  }

}

object Conveyor {
  sealed trait Service
  case object TransitPeople extends Service
  case object TransitLoad extends Service
  case object PlaceSatelliteOrbit extends Service

  def getKeyFromService(service: Service): String = {
    service match {
      case TransitLoad => s"transitload.conveyor"
      case TransitPeople => s"transitpeople.conveyor"
      case PlaceSatelliteOrbit => s"placesatelliteorbit.conveyor"
    }
  }

  implicit val serviceDecoder: Decoder[Service] = deriveDecoder[Service]
  implicit val serviceEncoder: Encoder[Service] = deriveEncoder[Service]
}
