package com.arcusys.valamis.learningpath.web.servlets.learningpath

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.Random

class LearningPathLogoTest extends LPServletTestBase {

  test("upload logo") {
    val lpId = createLearningPath("test 1")

    put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9),
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      body should haveJson(
        """ {"logoUrl":"/file/6ee1ac1e-eb0e-48e8-8b16-9eb7fa55505d.png"} """,
        ignoreValues = Seq("logoUrl")
      )
    }
  }

  test("upload logo with bmp format") {
    val lpId = createLearningPath("test 1")

    put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9),
      headers = Map("Content-Type" -> "image/bmp", "Content-Disposition" -> """attachment; filename="my.bmp"""")
    ) {
      status should beOk

      body should haveJson(
        """ {"logoUrl":"/file/6ee1ac1e-eb0e-48e8-8b16-9eb7fa55505d.bmp"} """,
        ignoreValues = Seq("logoUrl")
      )

      (parse(body) \ "logoUrl").extract[String] should endWith(".bmp")
    }
  }

  test("upload response should contains logo url") {
    val fileBytes = new Array[Byte](100)
    Random.nextBytes(fileBytes)

    val lpId = createLearningPath("test 1")

    val logoUrl = put(s"/learning-paths/$lpId/draft/logo",
      body = fileBytes,
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }

    get("/" + logoUrl) {
      status should beOk

      header("Content-Type") should be("image/png")
      bodyBytes should be(fileBytes)
    }
  }

  test("learning path response should contains logo url") {

    val fileBytes = new Array[Byte](100)
    Random.nextBytes(fileBytes)

    val lpId = createLearningPath("test 1")

    val logoUrl = put(s"/learning-paths/$lpId/draft/logo",
      body = fileBytes,
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "activated": false,
           |  "title": "test 1",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z",
           |  "logoUrl": "$logoUrl"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("replace logo") {
    val lpId = createLearningPath("test 1")

    val logo1Url = put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9),
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }

    val logo2Url = put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 5, 8, 13, 21, 33),
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }

    get("/" + logo1Url) {
      status should beNotFound
    }

    get("/" + logo2Url) {
      status should beOk
      bodyBytes should be(Array[Byte](1, 2, 3, 5, 8, 13, 21, 33))
    }
  }

  test("delete logo") {
    val lpId = createLearningPath("test 1")

    val logoUrl = put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9),
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }

    delete(s"/learning-paths/$lpId/draft/logo") {
      status should beNoContent
    }

    get("/" + logoUrl) {
      status should beNotFound
      body should haveJson(""" {"message":"file not found"} """)
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "activated": false,
           |  "title": "test 1",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("change draft logo should not affect on published version") {
    val lpId = createLearningPath("version 1 title")

    val originalLogo = setLogoToDraft(lpId, Array[Byte](1, 2, 3))

    publish(lpId)

    createNewDraft(lpId)

    val draftLogo = setLogoToDraft(lpId, Array[Byte](4, 5, 6))

    get(s"/learning-paths/$lpId/") {
      status should beOk
      body should haveJson(s"""{ "logoUrl": "$originalLogo" }""")
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk
      body should haveJson(s"""{ "logoUrl": "$draftLogo" }""")
    }
  }

  test("set logo should return 404 if no draft exists") {
    val lpId = createLearningPath("version 1 title")
    publish(lpId)

    put(s"/learning-paths/$lpId/draft/logo",
      body = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9),
      headers = Map("Content-Type" -> "image/png", "Content-Disposition" -> """attachment; filename="my.png"""")
    ) {
      status should beNotFound
    }
  }
}
