package app.abstractions

import collection.immutable.SortedMap

case class OutputShape(
  donors: SortedMap[String, List[Donation]],
  campaigns: SortedMap[String, List[Donation]]
)