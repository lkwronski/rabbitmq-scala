package agency

import conveyor.Conveyor

case class Ticket(nameAgency: String, id: String, service: Conveyor.Service) {
  def getMessage() : String = {
    s"Agency: $nameAgency, ticketID: $id, service: $service"
  }
}
