package api.twilio.server.scaladsl.actors

import akka.NotUsed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import api.twilio.server.scaladsl.actors.CreditDispatcher.{CreditMessage, CreditReport, CreditStatus, ReportData, ReportSecurity, StatusData}
import api.twilio.server.scaladsl.data.DataCacher

object Supervisor {

  val dispatcherMap: Map[String, ActorRef[CreditMessage]] = Map.empty

  trait DispatchType
  case class GetReport(security: ReportSecurity, data: ReportData) extends DispatchType
  case class GetStatus(data: StatusData) extends DispatchType

  case class SimpleDispatch(reqId: String) extends DispatchType

  case class CompleteDispatch(reqId: String) extends DispatchType

  def apply(): Behavior[DispatchType] = Behaviors.receive { (context, message) =>
      message match {
        case GetReport(s, d) =>
          val dispatcherActor = context.spawn(CreditDispatcher(), "actor-"+d.pClaimId)
//          context.log.info("actor spawned")
          dispatcherActor ! CreditReport(s, d, context.self)
          dispatcherMap + d.pClaimId -> dispatcherActor
        case GetStatus(d) =>
          val dispatcher = dispatcherMap.getOrElse(d.pClaimId, null)
          if (dispatcher == null)
            println("\"Dispatcher not fount with - {}\", d.pClaimId")
//            context.log.info("Dispatcher not fount with - {}", d.pClaimId)
          else dispatcher ! CreditStatus(d, context.self)
        case SimpleDispatch(i) =>
          DataCacher.saveResult(i)
        case _ =>
          println("Invalid DispatchType!")
//          context.log.info("Invalid DispatchType!")
      }

      Behaviors.same
  }

}
