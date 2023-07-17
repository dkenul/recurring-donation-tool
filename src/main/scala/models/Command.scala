package app.models

import app.helpers.DataManager
import app.abstractions._

sealed trait Command {
  def execute: Either[FailedExecutionError, DataShape]
}

// Register new commands
object Command {
  val addDonorPattern = """Add Donor (\S+) (\D+)(\d+(\.\d{1,2})?)""".r
  val donatePattern = """Donate (\S+) (\S+) (\D+)(\d+(\.\d{1,2})?)""".r

  private def isValidCurrency (symbol: String): Either[InvalidCommandError, Unit] = symbol match {
    case "$" => Right(())
    case _ => Left(InvalidCommandError(s"Invalid Command: $symbol is not a supported currency symbol"))
  }
  
  def apply(commandString: String): Either[CommandError, Command] = commandString match {
    // valid commands
    case addDonorPattern(name, currency, monthlyLimit, _) =>
      isValidCurrency(currency).map(_ => AddDonor(name, BigDecimal(monthlyLimit)))
    case s"Add Campaign $name" => 
      Right(AddCampaign(name))
    case donatePattern(dName, cName, currency, amount, _) => 
      isValidCurrency(currency).map(_ => Donate(dName, cName, BigDecimal(amount)))

    // unrecognized commands
    case cmd => Left(InvalidCommandError(s"Unrecognized Command: $cmd"))
  }
}

case class AddDonor(name: String, monthlyLimit: BigDecimal) extends Command {
  def execute: Either[FailedExecutionError, User] = {
    DataManager.createUser(name, monthlyLimit).left.map(FailedExecutionError.apply)
  }
}

case class AddCampaign (name: String) extends Command {
  def execute: Either[FailedExecutionError, Campaign] = {
    DataManager.createCampaign(name).left.map(FailedExecutionError.apply)
  }
}

case class Donate(userName: String, campaignName: String, amount: BigDecimal) extends Command {
  def execute: Either[FailedExecutionError, Donation] = {
    DataManager.createDonation(userName, campaignName, amount).left.map(FailedExecutionError.apply)
  }
}