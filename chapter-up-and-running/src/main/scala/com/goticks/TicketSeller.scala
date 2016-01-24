package com.goticks

import akka.actor.{ Actor, Props, PoisonPill }
import org.slf4j.{LoggerFactory, Logger}

object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))

  case class Add(tickets: Vector[Ticket]) 
  case class Buy(tickets: Int) 
  case class Ticket(id: Int) 
  case class Tickets(event: String,
                     entries: Vector[Ticket] = Vector.empty[Ticket]) 
  case object GetEvent 
  case object Cancel 

}

class TicketSeller(event: String) extends Actor {
  import TicketSeller._

  var tickets = Vector.empty[Ticket] 

  override def preStart() = {
    log.debug(s"Ticket seller actor for $event started.")
  }

  def receive = {
    case Add(newTickets) => tickets = tickets ++ newTickets
    case Buy(nrOfTickets) =>
      log.debug(s"Buying $nrOfTickets to ")
      val entries = tickets.take(nrOfTickets).toVector
      if(entries.size >= nrOfTickets) {
        sender() ! Tickets(event, entries)
        tickets = tickets.drop(nrOfTickets)
      } else sender() ! Tickets(event)
    case GetEvent =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
    case Cancel =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
      self ! PoisonPill
  }

  private lazy val log: Logger = LoggerFactory.getLogger(this.getClass)

}
