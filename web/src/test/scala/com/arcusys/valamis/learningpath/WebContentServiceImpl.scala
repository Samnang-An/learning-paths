package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.models.{WebContent, WebContentSort, WebContents}
import com.arcusys.valamis.learningpath.services.WebContentService

import scala.concurrent.Future

class WebContentServiceImpl(webContents: Seq[(WebContent, String)] = Nil)
  extends WebContentService {

  override def getAll(skip: Int, take: Int, sort: WebContentSort.Value, title:Option[String] = None )
                     (implicit companyId: Long): WebContents = {
    WebContents(webContents.map(_._1), webContents.size)
  }

  override def getWebContentTitle(id: Long): Future[Option[String]] = Future.successful{
    webContents.find(_._1.id == id).map(_._1.title)
  }

  override def getContent(id: Long): Future[Option[String]] = Future.successful{
    webContents.find(_._1.id == id).map(_._2)
  }

  override def getWebContentIdByClassPK(classPK: Long): Long = 1L

  override def addCheckerTask(webContentId: Long,
                              userId: Long,
                              companyId: Long): Unit = Unit
}
