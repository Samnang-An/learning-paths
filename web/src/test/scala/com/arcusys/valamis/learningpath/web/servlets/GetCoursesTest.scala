package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.Course
import com.arcusys.valamis.learningpath.{CourseServiceTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.services.CourseService

/**
  * Created by mminin on 01/03/2017.
  */
class GetCoursesTest extends LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    override def courseService: CourseService = new CourseServiceTestImpl(
      Seq(
        Course(1, "course_1", "http://c1.org"),
        Course(2, "course_2", "http://c1.org/v2"),
        Course(3, "course_3", "http://c1.org/v3"),
        Course(4, "abc", "http://c1.org/v4")
      )
    )
  }

  test("get courses") {
    get(s"/courses") {
      status should beOk
      body should haveJson(
        """{
          |  "items": [
          |    {"id":4, "title":"abc", "friendlyUrl": "http://c1.org/v4" },
          |    {"id":1, "title":"course_1", "friendlyUrl": "http://c1.org"},
          |    {"id":2, "title":"course_2", "friendlyUrl": "http://c1.org/v2"},
          |    {"id":3, "title":"course_3", "friendlyUrl": "http://c1.org/v3"}
          |  ],
          |  "total":4
          |}""".stripMargin)
    }
  }

  test("get courses with pagination") {
    get(s"/courses?skip=1&take=2") {
      status should beOk
      body should haveJson(
        """{
          |  "items": [
          |    {"id":1, "title":"course_1", "friendlyUrl": "http://c1.org"},
          |    {"id":2, "title":"course_2", "friendlyUrl": "http://c1.org/v2"}
          |  ],
          |  "total": 4
          |}""".stripMargin)
    }
  }

  test("get courses with filter") {
    get(s"/courses?title=abc") {
      status should beOk
      body should haveJson(
        """{
          |  "items": [
          |    {"id":4, "title":"abc", "friendlyUrl": "http://c1.org/v4" }
          |  ],
          |  "total":1
          |}""".stripMargin)
    }
  }

  test("get courses with filter sorting by title") {
    get(s"/courses?title=course&sort=title") {
      status should beOk
      body should haveJson(
        """{
          |  "items": [
          |    {"id":1, "title":"course_1", "friendlyUrl": "http://c1.org"},
          |    {"id":2, "title":"course_2", "friendlyUrl": "http://c1.org/v2"},
          |    {"id":3, "title":"course_3", "friendlyUrl": "http://c1.org/v3"}
          |  ],
          |  "total":3
          |}""".stripMargin)
    }
  }

  test("get courses with filter sorting by title desc") {
    get(s"/courses?title=course&sort=-title") {
      status should beOk
      body should haveJson(
        """{
          |  "items": [
          |    {"id":3, "title":"course_3", "friendlyUrl": "http://c1.org/v3"},
          |    {"id":2, "title":"course_2", "friendlyUrl": "http://c1.org/v2"},
          |    {"id":1, "title":"course_1", "friendlyUrl": "http://c1.org"}
          |  ],
          |  "total":3
          |}""".stripMargin)
    }
  }

  test("get existed course by id") {
    get(s"/courses/2") {
      status should beOk
      body should haveJson(
        """{"id":2,"title":"course_2"}""")
    }
  }

  test("get not existed course by id should return 404") {
    val fakeId = 222
    get(s"/courses/$fakeId") {
      status should beNotFound
      body should haveJson(s""" { "message": "no course with id: $fakeId"} """)
    }
  }

}
