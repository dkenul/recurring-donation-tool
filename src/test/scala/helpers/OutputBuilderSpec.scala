package tests

import collection.immutable.SortedMap
import app.helpers.OutputBuilder
import OutputBuilder._
import app.abstractions._

extension (t: Tuple3[String, String, String])
  def toDonation: Donation = Donation(t._1, t._2, BigDecimal(t._3))

class OutputBuilderSpec extends BaseSpec {  
  val emptyDonations = List[Donation]()
  val singleDonations = List(("A", "B", "100")).map(_.toDonation)
  val decimalDonations = List(("A", "B", "0.10"), ("A", "B", "0.20")).map(_.toDonation)
  val unsortedDonations = List(("Z", "C", "1"), ("Y", "B", "2"), ("X", "A", "3")).map(_.toDonation)
  
  
  describe ("OutputBuilder") {
    
    describe ("sumDonations") {
      it ("should return 0 for an empty list") {
        sumDonations(emptyDonations) shouldEqual BigDecimal("0")
      }

      it ("should not produce precision errors when adding decimals") {
        sumDonations(decimalDonations) shouldEqual BigDecimal("0.30")
      }
    }

    describe ("averageDonations") {
      it ("should return 0 for an empty list") {
        averageDonations(emptyDonations) shouldEqual BigDecimal("0")
      }

      it ("should handle a single donation") {
        averageDonations(singleDonations) shouldEqual BigDecimal("100")
      }

      it ("should handle a whole number average") {
        averageDonations(List(("A", "B", "100"), ("A", "B", "300")).map(_.toDonation)) shouldEqual BigDecimal("200")
      }

      it ("should round (up) decimal averages to two decimal points") {
        // exact average is 0.275
        averageDonations(List(("A", "B", "0.20"), ("A", "B", "0.35")).map(_.toDonation)) shouldEqual BigDecimal("0.28")
      }
    }
  }
}