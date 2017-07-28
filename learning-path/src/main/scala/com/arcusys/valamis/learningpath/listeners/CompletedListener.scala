package com.arcusys.valamis.learningpath.listeners


import scala.concurrent.Future

/**
  * Created by pkornilov on 3/16/17.
  */
trait CompletedListener {

  def onCompleted(userId: Long, entityId: Long)(implicit companyId: Long): Future[Unit]
}
