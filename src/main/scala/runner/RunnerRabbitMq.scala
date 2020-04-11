package runner

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, Envelope}
import conifg.RabbitMqConfig
import io.circe.{Json, Printer}
import message.JsonMessage

trait RunnerRabbitMq {

  def config: RabbitMqConfig

  val factory = new ConnectionFactory
  factory.setHost(config.host)
  val connection: Connection = factory.newConnection
  val channel: Channel = connection.createChannel

  protected def decodeMessage(body:  Array[Byte]): JsonMessage = {
    val message = new String(body, "UTF-8")
    io.circe.parser.decode(message)(JsonMessage.jsonMessageDecoder) match {
      case Right(obj) => obj
      case Left(exc)  => throw exc
    }
  }


  protected def bindQueue(queueName: String, routingKey: String): Unit ={
    channel.queueDeclare(queueName, false, false, false, null).getQueue
    channel.queueBind(queueName, config.exchange, routingKey)
  }

  private lazy val jsonPrinter: Printer = Printer(dropNullValues = true, indent = "")

  protected def encodeMessage(jsonMessage: JsonMessage): String = {
    val json = JsonMessage.jsonMessageEncoder(jsonMessage)
    jsonPrinter.print(json)
  }

  protected def basicAck(envelope: Envelope) = {
    val deliveryTag = envelope.getDeliveryTag
    channel.basicAck(deliveryTag, false)
  }
}
