package lgasior.example.domain

sealed trait GameEvent {
  def gameId: String
}
object GameEvent {
  case class PlayerJoined(override val gameId: String, playerId: String) extends GameEvent
}
