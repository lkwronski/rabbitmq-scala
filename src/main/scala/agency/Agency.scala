package agency

import conveyor.Conveyor

case class Agency( name: String) {

  def generateTicket(service: Conveyor.Service): Ticket = {
    val ticketId = generateTicketId()
    Ticket(name, ticketId, service)
  }

  private def generateTicketId() = java.util.UUID.randomUUID.toString

}
