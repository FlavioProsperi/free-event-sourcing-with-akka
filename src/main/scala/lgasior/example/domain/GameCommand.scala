package lgasior.example.domain

sealed trait GameCommand
object GameCommand {
  case class Join(playerId: String) extends GameCommand
}
