package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.services.{CertificateNotificationService, LPStatementService}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import com.arcusys.valamis.learningpath.services.impl.tables.{LPMemberTableComponent, LeaningPathTableComponent}
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.members.picker.service.{LiferayHelper, MemberService}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 22/02/2017.
  */
class LPMemberServiceImpl(val db: JdbcBackend#DatabaseDef,
                          val profile: JdbcProfile,
                          val actions: DbActions,
                          val liferay: LiferayHelper,
                          val taskManager: TaskManager,
                          val certificateNotificationService: CertificateNotificationService,
                          val userLPStatusModelListener: UserLPStatusModelListener,
                          val lpStatementService: LPStatementService)
                         (implicit val executionContext: ExecutionContext)
  extends MemberService
    with LPMemberTableComponent
    with LeaningPathTableComponent
    with SlickProfile
    with LPMemberServiceAddComponent
    with LPMemberServiceDeleteComponent {
}
