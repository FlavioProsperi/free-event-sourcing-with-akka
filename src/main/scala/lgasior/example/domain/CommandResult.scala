package lgasior.example.domain

sealed trait CommandResult
case class CommandError(msg: String) extends CommandResult
case object CommandSuccess extends CommandResult
