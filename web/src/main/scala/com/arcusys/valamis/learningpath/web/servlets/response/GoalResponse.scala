package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.GoalTypes

/**
  * Created by mminin on 21/02/2017.
  */
trait GoalResponse {
  def goalType: GoalTypes.Value
  def indexNumber: Int
}
