package com.arcusys.valamis.learningpath.models

// Not forget add same in valamis
object CertificateActivitesType extends Enumeration {
  val UserJoined = Value(0, "UserJoined")
  val Achieved = Value(1, "Success") // like was with old certificates
  val Published = Value(2, "Published") // 2 - old certificate publish activity type
  val Expired = Value(3, "Overdue")
  val Failed = Value(4, "Failed")
}
