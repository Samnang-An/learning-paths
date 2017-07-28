package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[old] trait LongKeyTableComponent { self: SlickProfile =>
  import profile.api._

  abstract class LongKeyTable[E](tag: Tag, name: String) extends Table[E](tag, name) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  }
}

