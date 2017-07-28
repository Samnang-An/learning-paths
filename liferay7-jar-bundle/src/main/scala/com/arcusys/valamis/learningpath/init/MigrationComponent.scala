package com.arcusys.valamis.learningpath.init

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.migration.{CompetencesMigration, CurriculumToLPMigration, HistoryTablesMigration}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle
import com.liferay.portal.kernel.service.{OrganizationLocalServiceUtil, RoleLocalServiceUtil, UserGroupLocalServiceUtil, UserLocalServiceUtil}
import org.osgi.service.component.annotations.{Activate, Component, Modified, Reference}

/**
  * Created by pkornilov on 3/27/17.
  */
@Component(immediate = true, service = Array(classOf[MigrationComponent]))
class MigrationComponent extends LiferayLogSupport { self =>

  private def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean = {
    val member = memberType match {
      case MemberTypes.User => UserLocalServiceUtil.fetchUser(memberId)
      case MemberTypes.Role => RoleLocalServiceUtil.fetchRole(memberId)
      case MemberTypes.UserGroup => UserGroupLocalServiceUtil.fetchUserGroup(memberId)
      case MemberTypes.Organization => OrganizationLocalServiceUtil.fetchOrganization(memberId)
    }
    Option(member).isDefined
  }

  @Activate
  @Modified
  protected def activate(properties: java.util.Map[String, Object]): Unit = {

    import Configuration.executionContext

    new CurriculumToLPMigration(
      Configuration.dbInfo, Configuration.dbActions,
      Configuration.liferayHelper, Configuration.companyService,
      Configuration.assetEntryService,
      Configuration.logoFileStorage, log) {
      override def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean =
        self.isMemberExisted(memberId, memberType)
    }
      .run()

    new HistoryTablesMigration(Configuration.dbInfo, log).run()

    new CompetencesMigration(Configuration.dbInfo, log).run()
  }

  //These methods are needed to make sure,
  //that needed Liferay services have been initialized before activating our component
  @Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED, unbind = "-")
  protected def setModuleServiceLifecycle(moduleServiceLifecycle: ModuleServiceLifecycle) {}

}
