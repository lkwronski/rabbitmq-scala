package conveyor

import com.rabbitmq.client._
import conifg.RabbitMqConfig

case class RunnerConveyor(service: Conveyor.Service, config: RabbitMqConfig) {

  val factory = new ConnectionFactory
  factory.setHost(config.host)
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel

  scribe.info(s"""Init runner Conveyor with service: $service""")

  channel.exchangeDeclare(config.exchange, BuiltinExchangeType.TOPIC)

  val queueName: String = channel.queueDeclare(service.toString.toLowerCase, false, false, false, null).getQueue
  val routingKey = s"${service.toString.toLowerCase}.conveyor"
  channel.queueBind(queueName, config.exchange, routingKey)
  channel.basicQos(1)

  scribe.info(s"""Bind queue with key: $routingKey""")

  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
      val message = new String(body, "UTF-8")
      scribe.info(s"""Conveyor receive ticket: service: $service received: $message""")
      val deliveryTag = envelope.getDeliveryTag
      channel.basicAck(deliveryTag, false)
    }
  }

  def consume(): Unit ={
    channel.basicConsume(queueName, false, consumer)
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
}