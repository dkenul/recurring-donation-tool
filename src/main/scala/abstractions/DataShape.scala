package app.abstractions

sealed trait DataShape

case class User (name: String, monthlyLimit: BigDecimal) extends DataShape
case class Campaign (name: String) extends DataShape
case class Donation (userName: String, campaignName: String, amount: BigDecimal) extends DataShape