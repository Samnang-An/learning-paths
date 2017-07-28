package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.members.picker.model.RangeResult

/**
  * Created by mminin on 28/02/2017.
  */
object RangeResponse {
  def apply[T](result: RangeResult[T]): Any = {
    Map("items" -> result.records, "total" -> result.total)
  }

  def apply[T](items: Seq[T], total: Int): Any = {
    Map("items" -> items, "total" -> total)
  }
}
