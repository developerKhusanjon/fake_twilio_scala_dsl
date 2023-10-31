package api.twilio.server.scaladsl.data

object Requests {
  case class CreditReportSecurity(pLogin: String, pPassword: String)

  case class CreditReportData(pHead: String, pCode: String, pClaimId: String, pReportId: Int, pReportFormat: Int)

  case class CreditReport(security: CreditReportSecurity, data: CreditReportData)

  case class CreditStatusData(pHead: String, pCode: String, pToken: String, pClaimId: String, pReportId: Int, pReportFormat: Int)

  case class CreditStatus(data: CreditStatusData)
}
