package agency

import conveyor.Conveyor
import io.circe._
import io.circe.generic.semiauto._

case class Ticket(nameAgency: String, id: String, service: Conveyor.Service) {
  def getMessage() : String = {
    s"Agency: $nameAgency, ticketID: $id, service: $service"
  }
}

object Ticket{
  implicit val ticketDecoder: Decoder[Ticket] = deriveDecoder
  implicit val ticketEncoder: Encoder[Ticket] = deriveEncoder
}
