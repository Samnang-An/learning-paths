package com.arcusys.valamis.learningpath.services

import java.io.InputStream

import scala.concurrent.Future


/**
  * Created by mminin on 13/02/2017.
  */
trait FileStorage {

  def get(fileName: String)
         (implicit companyId: Long): Future[Option[InputStream]]

  def add(fileName: String, inputStream: InputStream)
         (implicit companyId: Long): Future[Unit]

  def delete(fileName: String)
            (implicit companyId: Long): Future[Unit]

  def delete(fileNames: Seq[String])
            (implicit companyId: Long): Future[Unit]
}
