package lgasior.example.actor

import akka.actor.{ActorRef, Stash, Actor}
import lgasior.example.domain.GameEvent
import lgasior.example.domain.dsl._
import lgasior.example.utils.FreeUtils
import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scalaz.concurrent.Task
import scalaz._
import Scalaz._

object CommandHandlerInterpreter {
  private case object InvitationsServiceTimeout
}

trait CommandHandlerInterpreter {
  this: Actor with Stash =>

  import context.{dispatcher, system}
  import FreeUtils.InterpreterImplicits._
  import CommandHandlerInterpreter._

  def invitationsService: ActorRef

  type StateTask[A] = StateT[Task, Seq[GameEvent], A]

  private val eventsOpStateInterpreter = new (EventsOp ~> State[Seq[GameEvent], ?]) {
    override def apply[A](eventOp: EventsOp[A]): State[Seq[GameEvent], A] = eventOp match {
      case EventsOp.CreateEvent(event) => State { m => (m :+ event, ()) }
    }
  }

  private val invitationsOpTaskInterpreter = new (InvitationsOp ~> Task) {
    override def apply[A](invitationsOp: InvitationsOp[A]): Task[A] = invitationsOp match {
      case InvitationsOp.IsInvited(playerId, gameId) =>
        Task.async[A] { register =>
          invitationsService ! InvitationsServiceActor.GetPlayerInvitations(playerId)
          val scheduledTimeout = system.scheduler.scheduleOnce(2.seconds, self, InvitationsServiceTimeout)
          def complete(value: \/[Throwable, A]) = {
            register(value)
            scheduledTimeout.cancel()
            unstashAll()
            context.unbecome()
          }
          context.become({
            case InvitationsServiceActor.PlayerInvitations(gamesInvitedTo) =>
              complete(gamesInvitedTo.contains(gameId).right)
            case InvitationsServiceTimeout =>
              complete(new RuntimeException("Invitations service timeout").left)
            case msg if sender() == invitationsService =>
              complete(new RuntimeException("Unexpected response from invitations service").left)
            case _ =>
              stash()
          }, discardOld = false)
        }
    }
  }

  private implicit val invitationsOp2StateTask = new (InvitationsOp ~> StateTask) {
    override def apply[A](fa: InvitationsOp[A]): StateTask[A] =
      StateT(s => invitationsOpTaskInterpreter(fa).map(p => (s, p)))
  }

  private implicit val eventsOp2StateTask = new (EventsOp ~> StateTask) {
    override def apply[A](fa: EventsOp[A]): StateTask[A] =
      StateT(s => Task.now(eventsOpStateInterpreter(fa)(s)))
  }

  val interpreter = implicitly[CommandHandlerOp ~> StateTask]

}
