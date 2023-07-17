package app.abstractions

sealed trait CommandError {
  def message: String
}

case class InvalidCommandError(message: String) extends CommandError
case class FailedExecutionError(message: String) extends CommandError