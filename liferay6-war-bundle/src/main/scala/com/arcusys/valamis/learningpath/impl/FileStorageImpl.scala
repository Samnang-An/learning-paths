package com.arcusys.valamis.learningpath.impl

import java.io.InputStream

import com.arcusys.valamis.learningpath.services.FileStorage
import com.liferay.portal.model.CompanyConstants
import com.liferay.portlet.documentlibrary.store.DLStoreUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 14/02/2017.
  */
class FileStorageImpl(directory: String)
                     (implicit executionContext: ExecutionContext)
  extends FileStorage {

  val repositoryId = CompanyConstants.SYSTEM

  override def get(fileName: String)(implicit companyId: Long): Future[Option[InputStream]] = Future {
    Some {
      directory + "/" + fileName
    } filter { name =>
      DLStoreUtil.hasFile(companyId, repositoryId, name)
    } map { name =>
      DLStoreUtil.getFileAsStream(companyId, repositoryId, name)
    }
  }

  override def add(fileName: String, inputStream: InputStream)(implicit companyId: Long): Future[Unit] = Future {
    verifyDirectory()

    DLStoreUtil.addFile(companyId, repositoryId, directory + "/" + fileName, inputStream)
  }

  private def verifyDirectory()(implicit companyId: Long): Unit = {
    if (!DLStoreUtil.hasDirectory(companyId, repositoryId, directory)) {
      DLStoreUtil.addDirectory(companyId, repositoryId, directory)
    }
  }

  override def delete(fileName: String)
                     (implicit companyId: Long): Future[Unit] = Future {
    DLStoreUtil.deleteFile(companyId, repositoryId, directory + "/" + fileName)
  }

  override def delete(fileNames: Seq[String])
                     (implicit companyId: Long): Future[Unit] = Future {
    fileNames.foreach { fileName =>
      DLStoreUtil.deleteFile(companyId, repositoryId, directory + "/" + fileName)
    }
  }
}
