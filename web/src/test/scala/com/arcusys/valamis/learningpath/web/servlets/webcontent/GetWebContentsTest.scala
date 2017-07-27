package com.arcusys.valamis.learningpath.web.servlets.webcontent

import com.arcusys.valamis.learningpath.models.WebContent
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{ServletImpl, WebContentServiceImpl}

class GetWebContentsTest extends LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    override val webContentService = new WebContentServiceImpl(Seq(
      (WebContent(1, "WC 1"), "test content 1"),
      (WebContent(2, "WC 2"), "test content 2")
    ))
  }

  test("get web contents") {
    get("/web-contents/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [
           |    { "id":1, "title":"WC 1" },
           |    { "id":2, "title":"WC 2" }
           |  ]
           |}""".stripMargin)
    }
  }

  test("get web contents body") {
    get("/web-contents/1") {
      status should beOk

      response.header("Content-Type") should startWith("text/html")

      body should equal("test content 1")
    }
  }

  test("get web contents with wrong id") {
    get("/web-contents/1000") {
      status should beNotFound
    }
  }
}
