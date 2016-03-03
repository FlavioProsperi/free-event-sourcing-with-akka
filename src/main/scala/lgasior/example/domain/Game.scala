package lgasior.example.domain

import lgasior.example.domain.dsl._
import lgasior.example.utils.FreeUtils
import scala.language.implicitConversions
import scalaz.{Scalaz, Inject, Free}
import Scalaz._

object Game {
  type CommandHandler = Free[CommandHandlerOp, CommandResult]
}

case class Game(id: String, players: Set[String], maxPlayers: Int) {

  import Game._
  import InvitationsOp._
  import EventsOp._
  import FreeUtils.LiftImplicit._

  type Validate = CommandHandler => CommandHandler

  implicit def liftError[A](err: CommandError): CommandHandler = Free.point(err)

  def handleCommand(cmd: GameCommand)
                   (implicit I0: Inject[InvitationsOp, CommandHandlerOp],
                             I1: Inject[EventsOp, CommandHandlerOp]): CommandHandler = cmd match {
    case GameCommand.Join(playerId) =>

      val validateNotAlreadyJoined: Validate = next =>
        if (players.contains(playerId)) CommandError("Player already joined")
        else next

      val validateMaxPlayers: Validate = next =>
        if (players.size >= maxPlayers) CommandError("Max number of players reached")
        else next

      val validateInvited: Validate = next =>
        IsInvited(playerId, id) flatMap {
          case false => CommandError("Player is not invited")
          case true => next
        }

      (validateNotAlreadyJoined ∘ validateMaxPlayers ∘ validateInvited) {
        for {
          _ <- CreateEvent(GameEvent.PlayerJoined(id, playerId))
        } yield CommandSuccess
      }
  }

  def applyEvent(event: GameEvent): Game = event match {
    case GameEvent.PlayerJoined(_, playerId) =>
      copy(players = players + playerId)
  }

}
