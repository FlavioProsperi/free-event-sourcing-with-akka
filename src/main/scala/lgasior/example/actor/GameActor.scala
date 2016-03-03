package lgasior.example.actor

import akka.actor.ActorRef
import akka.persistence.PersistentActor
import lgasior.example.domain._
import scalaz._

class GameActor(id: String, override val invitationsService: ActorRef)
  extends PersistentActor with CommandHandlerInterpreter {

  override val persistenceId = s"game-$id"

  var state: Game = Game(id, Set.empty, maxPlayers = 4)

  override def receiveCommand = {
    case cmd: GameCommand =>
      val replyTo = sender()
      state.handleCommand(cmd)
        .foldMap(interpreter) // use interpreter to run computations
        .apply(Nil)           // initial state for events Seq
        .unsafePerformAsync { // run the Task
          case -\/(ex) =>
            replyTo ! CommandError("Unexpected error")
          case \/-((_, error: CommandError)) =>
            replyTo ! error
          case \/-((events, CommandSuccess)) =>
            persistAll(events)(applyEvent)
            deferAsync(()) { _ => replyTo ! CommandSuccess }
      }
  }

  override def receiveRecover = {
    case ev: GameEvent => applyEvent(ev)
  }

  def applyEvent(ev: GameEvent) = {
    state = state.applyEvent(ev)
  }

}
