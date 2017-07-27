package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, MessageBusServiceTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

class DeleteBigLearningPathTest extends {
  val user1Id = 101
  val user2Id = 102
  val user3Id = 103
  val group1Id = 201
  val group2Id = 202
} with LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    private val group1 = IdAndName(group1Id, "group1")
    private val group2 = IdAndName(group2Id, "group2")

    override val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Seq(group2), Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Seq(group1), Nil, Nil, Nil),
        ForcedUserInfo(user3Id, "user 3", "/logo/u3", Seq(group1, group2), Nil, Nil, Nil)
      ),
      userGroups = Seq(group1, group2)
    )
    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      Map(), Map(), isAssignmentDeployed = true,
      Map(), Map(), isLessonDeployed = true,
      Map(), isCourseDeployed = false
    )
  }

  test("delete big learning path") {
    val lpId = createLearningPath("path 1")

    val logoUrl = setLogoToDraft(lpId, Array[Byte](1, 3, 4, 5, 6))

    addMember(lpId, group1Id, MemberTypes.UserGroup)
    addMember(lpId, group2Id, MemberTypes.UserGroup)
    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)
    addMember(lpId, user3Id, MemberTypes.User)

    createGoalsTreeWithAllTypes(lpId)

    publish(lpId)
    createNewDraft(lpId)

    delete(s"/learning-paths/$lpId")(status should beNoContent)

    get(s"/learning-paths/$lpId")(status should beNotFound)

    get("/" + logoUrl)(status should beNotFound)
  }
}
