package com.arcusys.valamis.learningpath.impl

import java.util

import com.arcusys.valamis.learningpath.models.{Course, CourseSort}
import com.arcusys.valamis.learningpath.services.CourseService
import com.liferay.portal.model.{Group, GroupConstants}
import com.liferay.portal.service.GroupLocalServiceUtil
import com.liferay.portal.util.comparator.GroupNameComparator

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 28/02/2017.
  */
class CourseServiceImpl(implicit executionContext: ExecutionContext)
  extends CourseService {

  val courseParams = new util.LinkedHashMap[String, AnyRef](3)

  courseParams.put("site", Boolean.box(true))
  courseParams.put("active", Boolean.box(true))
  courseParams.put("types", List(
    Int.box(GroupConstants.TYPE_SITE_OPEN),
    Int.box(GroupConstants.TYPE_SITE_RESTRICTED),
    Int.box(GroupConstants.TYPE_SITE_PRIVATE)
  ).asJava)

  override def getCoursesCount(titleFilter: Option[String])
                              (implicit companyId: Long): Future[Int] = Future {
    GroupLocalServiceUtil.searchCount(companyId, titleFilter.orNull, courseParams)
  }

  override def getCourses(titleFilter: Option[String],
                          sort: CourseSort.Value,
                          skip: Int,
                          take: Int)
                         (implicit companyId: Long): Future[Seq[Course]] = Future {
    val orderComparator = sort match {
      case CourseSort.title => new GroupNameComparator(true)
      case CourseSort.titleDesc => new GroupNameComparator(false)
    }

    GroupLocalServiceUtil
      .search(companyId, titleFilter.orNull, courseParams, skip, skip + take, orderComparator)
      .asScala
      .map(extract)
  }

  override def getCourseTitlesByIds(ids: Seq[Long]): Future[Map[Long, String]] = Future {
    ids flatMap { id =>
      Option(GroupLocalServiceUtil.fetchGroup(id)) map { g =>
        (g.getGroupId, g.getDescriptiveName)
      }
    } toMap
  }

  override def getCourseById(id: Long): Future[Option[Course]] = Future {
    Option(GroupLocalServiceUtil.fetchGroup(id))
      .map(extract)
  }

  private def extract(group: Group): Course = {
    Course(group.getGroupId, group.getDescriptiveName, getCourseFriendlyUrl(group))
  }

  private def getCourseFriendlyUrl(group: Group): String = {
    group.getFriendlyURL match {
      case str: String if !str.isEmpty =>
        if (group.hasPublicLayouts) {
          s"/web$str"
        }
        else if (group.hasPrivateLayouts) {
          s"/group$str"
        }
        else ""

      case _ => ""
    }
  }
}
