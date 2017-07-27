package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.history.{LPSnapshot, UserStatusSnapshot}
import com.arcusys.valamis.learningpath.services.impl.tables.history.HistoryTableComponent
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 05/04/2017.
  */
class LPHistoryDBIOActions(val profile: JdbcProfile)
                          (implicit executionContext: ExecutionContext)
  extends HistoryTableComponent
    with SlickProfile {

  import profile.api._

  def insertLPSnapshot(lpSnapshot: LPSnapshot): DBIO[Int] = {
    lpHistoryTQ += lpSnapshot
  }

  def insertUserStatusSnapshot(userStatusSnapshot: UserStatusSnapshot): DBIO[Int] = {
    userStatusHistoryTQ += userStatusSnapshot
  }

  def insertUserStatusSnapshots(userStatusSnapshots: Seq[UserStatusSnapshot]): DBIO[Option[Int]] = {
    userStatusHistoryTQ ++= userStatusSnapshots
  }

}
