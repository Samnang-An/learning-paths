package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.models.{Course, CourseSort}
import com.arcusys.valamis.learningpath.services.CourseService

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 01/03/2017.
  */
class CourseServiceTestImpl(courses: Seq[Course] = Nil)
                           (implicit executionContext: ExecutionContext)
  extends CourseService {

  override def getCoursesCount(titleFilter: Option[String])
                              (implicit companyId: Long): Future[Int] = Future {
    titleFilter
      .map(_.toLowerCase)
      .fold {
        courses.length
      } { text =>
        courses.count(_.title.toLowerCase.indexOf(text) != -1)
      }
  }

  override def getCourses(titleFilter: Option[String],
                          sort: CourseSort.Value,
                          skip: Int,
                          take: Int)
                         (implicit companyId: Long): Future[Seq[Course]] = Future {
    val filtered = titleFilter
      .map(_.toLowerCase)
      .fold {
        courses
      } { text =>
        courses.filter(_.title.toLowerCase.indexOf(text) != -1)
      }

    val sorted = sort match {
      case CourseSort.title => filtered.sortBy(_.title)
      case CourseSort.titleDesc => filtered.sortBy(_.title).reverse
    }

    sorted.slice(skip, skip + take)
  }

  override def getCourseTitlesByIds(ids: Seq[Long]): Future[Map[Long, String]] = Future {
    val idSet = ids.toSet
    courses find {
      course => idSet(course.id)
    } map { course =>
      (course.id, course.title)
    } toMap
  }

  override def getCourseById(id: Long): Future[Option[Course]] = Future {
    courses find (_.id == id) map { course =>
      Course(course.id, course.title, course.friendlyUrl)
    }
  }
}
