package lgasior.example

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import lgasior.example.actor.{InvitationsServiceActor, GameActor}
import lgasior.example.domain.GameCommand
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Example extends App {

  implicit val system = ActorSystem("Example1-system")

  val invitationsService = system.actorOf(Props[InvitationsServiceActor])

  val gameActor = system.actorOf(Props(new GameActor("1", invitationsService)))

  implicit val timeout = Timeout(2.seconds)

  def join(playerId: String) =
    (gameActor ? GameCommand.Join(playerId)).onComplete { case res => println(s"Command result: $res") }

  join("invited-1")     // Success
  join("invited-2")     // Success
  join("invited-2")     // Error: Already joined
  join("not-invited-1") // Error: Not invited
  join("invited-3")     // Success
  join("invited-4")     // Success
  join("invited-5")     // Error: Max players reached

}
