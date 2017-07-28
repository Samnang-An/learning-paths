package com.arcusys.valamis.learningpath.services.impl.tables.utils

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 5/16/17.
  */
trait FilterUtils { self: SlickProfile =>

  implicit def executionContext: ExecutionContext

  import profile.api._

  /**
    * Workaround for Oracle restriction of maximum amount of elements inside SQL IN clause
    */
  def withFilterByIds[T](ids: Seq[Long])(action: Seq[Long] => DBIO[Seq[T]]): DBIO[Seq[T]] = {
    DBIO.sequence(ids.grouped(1000) map { ids => action(ids) }) map (_.toSeq.flatten)
  }

}
