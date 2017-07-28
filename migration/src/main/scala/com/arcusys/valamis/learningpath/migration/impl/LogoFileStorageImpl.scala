package com.arcusys.valamis.learningpath.migration.impl

import java.io.{ByteArrayInputStream, InputStream}

import com.arcusys.valamis.learningpath.services.FileStorage

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by pkornilov on 3/28/17.
  */
class LogoFileStorageImpl extends FileStorage {

  private var files = Map[String, Array[Byte]]()

  override def get(fileName: String)
                  (implicit companyId: Long): Future[Option[InputStream]] = Future {
    files.get(fileName).map(new ByteArrayInputStream(_))
  }

  override def add(fileName: String, inputStream: InputStream)
                  (implicit companyId: Long): Future[Unit] = Future {
    val data = Stream.continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray
    files = files + ((fileName, data))
  }

  override def delete(fileName: String)
                     (implicit companyId: Long): Future[Unit] = Future {
    files = files.filterKeys(_ != fileName)
  }

  override def delete(fileNames: Seq[String])
                     (implicit companyId: Long): Future[Unit] = Future {
    files = files.filterKeys(!fileNames.contains(_))
  }

}
