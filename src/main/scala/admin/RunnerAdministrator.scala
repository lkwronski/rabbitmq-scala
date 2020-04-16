package admin

import agency.Ticket
import com.rabbitmq.client.{AMQP, BuiltinExchangeType, DefaultConsumer, Envelope}
import conifg.RabbitMqConfig
import message.JsonMessage
import message.TypeMessage.{Approved, Commission, PlainText}
import runner.RunnerRabbitMq

case class Administrator(config: RabbitMqConfig) extends RunnerRabbitMq{

  scribe.info(s"""Init Administrator""")

  channel.exchangeDeclare(config.exchange, BuiltinExchangeType.TOPIC)

  val queueConveyor: String = "admin"
  val routingKeyConveyor = s"#.#"
  bindQueue(queueConveyor, routingKeyConveyor)
  channel.basicQos(1)

  scribe.info(s"""Bind queue with key: $routingKeyConveyor""")

  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
      val jsonMessage = decodeMessage(body)
      jsonMessage.typeMessage match {
        case Approved | Commission => consumeTicket(jsonMessage.ticket)
        case PlainText => _
      }
      val deliveryTag = envelope.getDeliveryTag
      channel.basicAck(deliveryTag, false)
    }
  }

  private def consumeTicket(ticket: Option[Ticket]) = {
    ticket match {
      case None => scribe.error("Empty ticket")
      case Some(t) => {
        scribe.info(s"""${t.getMessage()}""")
      }
    }
  }

  def consume(): Unit ={
    channel.basicConsume(queueConveyor, false, consumer)
  }


}
