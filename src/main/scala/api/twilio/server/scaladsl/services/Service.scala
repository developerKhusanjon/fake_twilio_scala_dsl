package api.twilio.server.scaladsl.services

import api.twilio.server.scaladsl.FakeTwilioServer.system
import api.twilio.server.scaladsl.actors.CreditDispatcher.{ReportData, ReportSecurity, StatusData}
import api.twilio.server.scaladsl.actors.Supervisor.{GetReport, GetStatus}
import api.twilio.server.scaladsl.data.DataCacher._
import api.twilio.server.scaladsl.data.Requests.{CreditReport, CreditStatus}

object Service {
  def sendReport(report: CreditReport) = {
    println("report service started!")
    system ! GetReport(ReportSecurity(report.security.pLogin, report.security.pPassword),
      ReportData(report.data.pHead, report.data.pCode, report.data.pClaimId, report.data.pReportId, report.data.pReportFormat))
    println("message sent to report actor!")
    getResult(report.data.pClaimId)
  }

  def checkStatus(status: CreditStatus) = {
    println("status checking started")
    system ! GetStatus(StatusData(status.data.pHead, status.data.pCode, status.data.pToken, status.data.pClaimId, status.data.pReportId, status.data.pReportFormat))
    println("message sent to status actor!")
    getResult(status.data.pClaimId)
  }
}
