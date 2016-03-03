package lgasior.example.domain

import lgasior.example.domain.dsl.{InvitationsOp, EventsOp, CommandHandlerOp}
import org.scalatest.{Matchers, FlatSpec}
import lgasior.example.utils.FreeUtils.InterpreterImplicits._

import scalaz.{~>, State}

class GameSpec extends FlatSpec with Matchers {

  type TestState[A] = State[Seq[GameEvent], A]

  def testInterpreter(isPlayerInvited: String => Boolean): CommandHandlerOp ~> TestState = {
    val eventsOpInterpreter = new (EventsOp ~> TestState) {
      override def apply[A](eventOp: EventsOp[A]): TestState[A] = eventOp match {
        case EventsOp.CreateEvent(event) => State { m => (m :+ event, ()) }
      }
    }

    val invitationsOpInterpreter = new (InvitationsOp ~> TestState) {
      override def apply[A](fa: InvitationsOp[A]): TestState[A] = fa match {
        case InvitationsOp.IsInvited(playerId, _) => State { m => (m, isPlayerInvited(playerId)) }
      }
    }

    eventsOpInterpreter or invitationsOpInterpreter
  }

  "Game" should "reject Join command if player is not invited" in {
    val playerId = "player1"
    val game = Game("1", Set.empty, maxPlayers = 1)

    val (_, result) = game
      .handleCommand(GameCommand.Join(playerId))
      .foldMap(testInterpreter(isPlayerInvited = _ => false))
      .apply(Nil)

    result shouldBe CommandError("Player is not invited")
  }

  it should "accept Join command if it's valid" in {
    val playerId = "player1"
    val game = Game("1", Set.empty, maxPlayers = 1)

    val (events, result) = game
      .handleCommand(GameCommand.Join(playerId))
      .foldMap(testInterpreter(isPlayerInvited = _ => true))
      .apply(Nil)

    result shouldBe CommandSuccess
    events should contain only GameEvent.PlayerJoined(game.id, playerId)
  }

}
