package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{WebContentSort, WebContents}

import scala.concurrent.Future

trait WebContentService {

  def getAll(skip: Int, take: Int, sort: WebContentSort.Value, title:Option[String] = None )
            (implicit companyId: Long): WebContents

  def getWebContentTitle(id: Long): Future[Option[String]]

  def getContent(id: Long): Future[Option[String]]

  def addCheckerTask(webContentId: Long,
                     userId: Long,
                     companyId: Long)

  def getWebContentIdByClassPK(classPK: Long): Long
}
