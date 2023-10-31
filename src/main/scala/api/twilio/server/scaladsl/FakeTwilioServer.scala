package api.twilio.server.scaladsl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onComplete, path, post}
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, jsonFormat1, jsonFormat2, jsonFormat3, jsonFormat4, jsonFormat5, jsonFormat6}
import spray.json.RootJsonFormat
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import api.twilio.server.scaladsl.actors.Supervisor
import api.twilio.server.scaladsl.actors.Supervisor.{DispatchType, GetReport}
import api.twilio.server.scaladsl.data.Requests
import api.twilio.server.scaladsl.data.Requests.{CreditReport, _}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object FakeTwilioServer {
  // needed to run the route
  implicit val system: ActorSystem[DispatchType] = ActorSystem(Supervisor(), "FakeTwilioServer")
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContext = system.executionContext
  implicit val timeout: Timeout = Timeout(2.minutes)

  var attempts: Map[String, Attempt] = Map.empty

  // domain model
  final case class Attempt(count: Byte, timeInMill: Long)

  final case class Sms(clientId: String, text: String)

  // formats for unmarshalling and marshalling
  implicit val orderFormat: RootJsonFormat[Sms] = jsonFormat2(Sms.apply)
  implicit val securityFormat: RootJsonFormat[CreditReportSecurity] = jsonFormat2(CreditReportSecurity.apply)
  implicit val dataFormat: RootJsonFormat[CreditReportData] = jsonFormat5(CreditReportData.apply)
  implicit val statusDataFormat: RootJsonFormat[CreditStatusData] = jsonFormat6(CreditStatusData.apply)
  implicit val reportFormat: RootJsonFormat[Requests.CreditReport] = jsonFormat2(Requests.CreditReport.apply)
  implicit val statusFormat: RootJsonFormat[Requests.CreditStatus] = jsonFormat1(Requests.CreditStatus.apply)

  import api.twilio.server.scaladsl.data.DataCacher._

  implicit val resultDataFormat: RootJsonFormat[ResultData] = jsonFormat4(ResultData.apply)
  implicit val resultFormat: RootJsonFormat[Result] = jsonFormat3(Result.apply)


  //  trait Status
  //  case object Successful extends Status
  //  case object Failed extends Status
  //  case object Blocked extends Status

  def sendSms(sms: Sms): Future[String] = {
    val currentTimeInMillis = System.currentTimeMillis()
    var lastAttempt: Attempt = Attempt(1, System.currentTimeMillis())

    if (attempts.contains(sms.clientId) &&
      System.currentTimeMillis() - attempts(sms.clientId).timeInMill < 60000)
      lastAttempt = attempts(sms.clientId)

    if (lastAttempt.count > 5) {
      Future("Blocked")
    } else if (currentTimeInMillis % 3 == 0) {
      attempts + (sms.clientId
        -> Attempt((lastAttempt.count + 1).toByte, lastAttempt.timeInMill))
      Future("Failure")
    } else {
      attempts + (sms.clientId -> Attempt(1, System.currentTimeMillis()))
      Future("Success")
    }
  }

  def main(args: Array[String]): Unit = {
    import api.twilio.server.scaladsl.services.Service._

    val route: Route =
      concat(
        get {
          path("health") {
            get {
              complete(StatusCodes.OK)
            }
          }
        },
        post {
          path("send-sms") {
            entity(as[Sms]) { sms =>
              onComplete(sendSms(sms)) {
                case Success(result) => result match {
                  case "Blocked" => complete(StatusCodes.TooManyRequests -> "Too many attempts... blocked for 60s")
                  case "Failure" => complete(StatusCodes.RetryWith -> "Failed..., please try again")
                  case "Success" => complete(StatusCodes.OK -> "Sms sent successfully!")
                }
                case Failure(ex) => complete(StatusCodes.InternalServerError -> "Internal Server error..., try later!")
              }
            }
          }
        },
        post {
          path("credit-report") {
            entity(as[CreditReport]) {report =>
             complete(sendReport(report))
            }
          }
        },
        post {
          path("credit-status") {
            entity(as[CreditStatus]) { status =>
              complete(checkStatus(status))
            }
          }
        }
      )

    val bindingFuture = Http().newServerAt("localhost", 8083).bind(route)
    println(s"Server online at http://localhost:8083/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
