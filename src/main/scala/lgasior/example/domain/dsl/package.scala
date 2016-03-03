package lgasior.example.domain

import scalaz.Coproduct

package object dsl {
  type CommandHandlerOp[A] = Coproduct[EventsOp, InvitationsOp, A]
}
