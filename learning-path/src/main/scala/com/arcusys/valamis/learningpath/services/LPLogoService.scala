package com.arcusys.valamis.learningpath.services

import java.io.InputStream

import scala.concurrent.Future

/**
  * Created by mminin on 13/02/2017.
  */
trait LPLogoService {

  def setDraftLogo(id: Long, fileName: String, stream: InputStream)
                  (implicit companyId: Long): Future[String]

  def deleteDraftLogo(id: Long)
                     (implicit companyId: Long): Future[Unit]

  def getLogo(logoName: String)
             (implicit companyId: Long): Future[Option[InputStream]]
}
