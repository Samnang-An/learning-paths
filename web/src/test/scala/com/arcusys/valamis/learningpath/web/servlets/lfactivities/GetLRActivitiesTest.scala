package com.arcusys.valamis.learningpath.web.servlets.lfactivities

import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.utils.LRActivityTypeServiceImpl

class GetLRActivitiesTest extends LPServletTestBase {

  override def servlet: ServletImpl = new ServletImpl(dbInfo) {
    override val lrActivityTypeService = new LRActivityTypeServiceImpl(
      Seq(
        LRActivityType("activityType1", "test_1"),
        LRActivityType("activityType2", "test_2")
      )
    )
  }

  test("get liferay activity types") {
    get("/lr-activity-types/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "activityName": "activityType1",
           |    "title": "test_1"
           |  },
           |  {
           |    "activityName": "activityType2",
           |    "title": "test_2"
           |  }
           |]""".stripMargin)
    }
  }
}
