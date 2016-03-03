package lgasior.example.domain.dsl

import lgasior.example.domain.GameEvent

sealed trait EventsOp[A]
object EventsOp {
  case class CreateEvent(event: GameEvent) extends EventsOp[Unit]
}
