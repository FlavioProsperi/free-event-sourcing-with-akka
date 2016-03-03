package lgasior.example.actor

import akka.actor.Actor

object InvitationsServiceActor {
  case class GetPlayerInvitations(playerId: String)
  case class PlayerInvitations(gamesInvitedTo: Set[String])
}

class InvitationsServiceActor extends Actor {

  import InvitationsServiceActor._

  val invitationsByPlayer: Map[String, Set[String]] =
    (1 to 5 map (i => s"invited-$i" -> Set("1"))).toMap

  override def receive = {
    case GetPlayerInvitations(playerId) =>
      sender() ! PlayerInvitations(invitationsByPlayer.getOrElse(playerId, Set.empty))
  }

}
