package lgasior.example.domain.dsl

sealed trait InvitationsOp[A]
object InvitationsOp {
  case class IsInvited(playerId: String, gameId: String) extends InvitationsOp[Boolean]
}
