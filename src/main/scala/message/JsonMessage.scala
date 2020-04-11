package message

import agency.Ticket
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class JsonMessage(typeMessage: TypeMessage, ticket: Option[Ticket])

object JsonMessage {
  implicit val jsonMessageDecoder: Decoder[JsonMessage] = deriveDecoder[JsonMessage]
  implicit val jsonMessageEncoder: Encoder[JsonMessage] = deriveEncoder[JsonMessage]
}

