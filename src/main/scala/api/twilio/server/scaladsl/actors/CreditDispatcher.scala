package api.twilio.server.scaladsl.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import api.twilio.server.scaladsl.actors.Supervisor.{CompleteDispatch, DispatchType, SimpleDispatch}

object CreditDispatcher {
  trait CreditMessage

  case class ReportSecurity(pLogin: String, pPassword: String)

  case class ReportData(pHead: String, pCode: String, pClaimId: String, pReportId: Int, pReportFormat: Int)

  case class CreditReport(security: ReportSecurity, data: ReportData, replyTo: ActorRef[DispatchType]) extends CreditMessage

  case class StatusData(pHead: String, pCode: String, pToken: String, pClaimId: String, pReportId: Int, pReportFormat: Int)

  case class CreditStatus(data: StatusData, replyTo: ActorRef[DispatchType]) extends CreditMessage



  def creditReportActor(): Behavior[CreditMessage] =
    Behaviors.receiveMessage {
      case CreditReport(security, data, replyTo) =>
        replyTo ! SimpleDispatch(data.pClaimId)
        creditStatusActor(System.currentTimeMillis())
      case _ => Behaviors.same
    }

  def creditStatusActor(time: Long): Behavior[CreditMessage] =
    Behaviors.receiveMessage {
      case CreditStatus(data, replyTo) =>
        if (System.currentTimeMillis() - time < 60_000) {
          replyTo ! SimpleDispatch(data.pClaimId)
          Behaviors.same
        } else {
          replyTo ! CompleteDispatch(data.pClaimId)
          creditReportActor
        }
      case _ => Behaviors.same
    }

 def apply() = creditReportActor()
}
