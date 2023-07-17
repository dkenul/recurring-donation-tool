package app.helpers

import collection.mutable
import collection.immutable.SortedMap

import app.abstractions._

object DataManager {
  /*
  * Ideally we would use a real database for persisting application data
  * However, we can create a lightweight mock with mutable data structures 
  * for convenience (and no external dependency)
  */
  var users = mutable.Map[String, User]()
  var campaigns = mutable.Map[String, Campaign]()
  var donationsByUser = mutable.Map[String, mutable.Buffer[Donation]]()
  var donationsByCampaign = mutable.Map[String, mutable.Buffer[Donation]]()

  def getUser (name: String): Option[User] = users.get(name)
  def getCampaign (name: String): Option[Campaign] = campaigns.get(name)

  def createUser (name: String, monthlyLimit: BigDecimal): Either[String, User] = {
    getUser(name) match {
      case Some(_) => Left(s"createUser failed - User: $name already exists")
      case _ =>
        val newUser = User(name, monthlyLimit)

        users.addOne(name -> newUser)

        Right(newUser)
    }
  }

  def createCampaign (name: String): Either[String, Campaign] = {
    getCampaign(name) match {
      case Some(_) => Left(s"createCampaign failed - Campaign: $name already exists")
      case _ =>
        val newCampaign = Campaign(name)

        campaigns.addOne(name -> newCampaign)

        Right(newCampaign)
    }
  }

  def createDonation (userName: String, campaignName: String, amount: BigDecimal): Either[String, Donation] = {
    val donation = Donation(userName, campaignName, amount)
    def prefix(s: String) = s"createDonation failed - $s"
    
    getUser(userName).fold(Left(prefix(s"User: $userName does not exist"))) { user =>
      getCampaign(campaignName).fold(Left(prefix(s"Campaign: $campaignName does not exist"))) { _ =>
        donationsByUser.getOrElse(user.name, mutable.Buffer[Donation]()).foldLeft(BigDecimal("0")) {
          case (total, previousDonation) if previousDonation.userName == userName => total + previousDonation.amount
          case (total, _) => total
        } match {
          case previousTotal if previousTotal + donation.amount > user.monthlyLimit =>
            Left(prefix(s"Amount: $amount for User: $userName would exceed monthlyLimit: ${user.monthlyLimit}"))
          case _ =>
            donationsByUser.getOrElseUpdate(donation.userName, mutable.Buffer[Donation]()) += donation
            donationsByCampaign.getOrElseUpdate(donation.campaignName, mutable.Buffer[Donation]()) += donation
  
            Right(donation)
        }
      }
    }
  }

  def transformData (): OutputShape = {
    OutputShape(
      users.foldLeft(SortedMap[String, List[Donation]]()) { case (map, (name, _)) =>
        map.updated(name, donationsByUser.get(name).fold(List[Donation]())(_.toList))
      },
      campaigns.foldLeft(SortedMap[String, List[Donation]]()) { case (map, (name, _)) =>
        map.updated(name, donationsByCampaign.get(name).fold(List[Donation]())(_.toList))
      }
    )
  }

  // for the sake of integration tests
  def reInitialize (): Unit = {
    users = mutable.Map[String, User]()
    campaigns = mutable.Map[String, Campaign]()
    donationsByUser = mutable.Map[String, mutable.Buffer[Donation]]()
    donationsByCampaign = mutable.Map[String, mutable.Buffer[Donation]]()
  }
}