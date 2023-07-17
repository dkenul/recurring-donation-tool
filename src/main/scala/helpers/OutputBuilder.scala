package app.helpers

import collection.immutable.SortedMap
import scala.util.{Try, Success, Failure}
import scala.math.BigDecimal.RoundingMode

import app.abstractions.{Donation, OutputShape}

object OutputBuilder {
  def sumDonations (donations: List[Donation]): BigDecimal = {
    donations.foldLeft(BigDecimal("0"))(_ + _.amount)
  }

  def averageDonations (donations: List[Donation]): BigDecimal = {
    donations
      .foldLeft((BigDecimal("0"), 1)) { case ((avg, i), donation) => (avg + (donation.amount - avg)/i, i + 1) }._1
      .setScale(2, RoundingMode.HALF_UP)
  }

  def formatCurrency (value: BigDecimal): String = {
    Try(value.setScale(0)) match {
      case Success(trimmedValue: BigDecimal) => trimmedValue.toString
      case _ => value.toString
    }
  }

  def formatDonors (donationsByDonor: SortedMap[String, List[Donation]]): String = {
    donationsByDonor
      .map((userName, donations) => 
        s"$userName: Total: $$${
          formatCurrency(sumDonations(donations))} Average: $$${
          formatCurrency(averageDonations(donations))}")
      .mkString("\n")
  }

  def formatCampaigns (donationsByCampaign: SortedMap[String, List[Donation]]): String = {
    donationsByCampaign
      .map((campaignName, donations) => 
        s"$campaignName: Total: $$${
          formatCurrency(sumDonations(donations))}")
      .mkString("\n")
  }

  def generateSummary (data: OutputShape): String = {
    s"""
    |Donors:
    |${formatDonors(data.donors)}
    |
    |Campaigns:
    |${formatCampaigns(data.campaigns)}
    """
      .trim
      .stripMargin
  }
}