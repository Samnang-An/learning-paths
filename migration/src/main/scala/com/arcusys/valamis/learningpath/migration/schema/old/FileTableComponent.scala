package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.FileRecord
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait FileTableComponent { self: SlickProfile =>

  import profile.api._

  class FileTable(tag : Tag) extends Table[FileRecord](tag, "LEARN_FILE") {
    def filename = column[String]("FILENAME", O.PrimaryKey, O.SqlType("varchar(255)"))
    def content = column[Option[Array[Byte]]]("CONTENT")

    def * = (filename, content) <> (FileRecord.tupled, FileRecord.unapply)
  }

  val files = TableQuery[FileTable]
}

