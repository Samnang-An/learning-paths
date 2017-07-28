package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.migration.schema.old.OldCurriculumTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

import scala.language.existentials

/**
  * Created by pkornilov on 4/1/17.
  */
trait MigrationQueries {
  self: SlickProfile with OldCurriculumTables =>

  import profile.api._

  val oldGoalsGroupsQ = Compiled { certificateId: Rep[Long] =>
    certificateGoalGroups filter { group =>
      group.isDeleted === false && group.certificateId === certificateId
    }
  }

  val oldGoalsQ = Compiled { certificateId: Rep[Long] =>
    certificateGoals filter { goal =>
      goal.isDeleted === false && goal.certificateId === certificateId
    }
  }

  val oldActivityGoalsQ = Compiled { goalId: Rep[Long] =>
    activityGoals filter (_.goalId === goalId)
  }

  val oldAssignmentGoalsQ = Compiled { goalId: Rep[Long] =>
    assignmentGoals filter (_.goalId === goalId)
  }

  val oldCourseGoalsQ = Compiled { goalId: Rep[Long] =>
    courseGoals filter (_.goalId === goalId)
  }

  val oldPackageGoalsQ = Compiled { goalId: Rep[Long] =>
    packageGoals filter (_.goalId === goalId)
  }

  val oldStatementGoalsQ = Compiled { goalId: Rep[Long] =>
    statementGoals filter (_.goalId === goalId)
  }

  val oldTrainingEventGoalsQ = Compiled { goalId: Rep[Long] =>
    trainingEventGoals filter (_.goalId === goalId)
  }

  val oldMembersQ = Compiled { certificateId: Rep[Long] =>
    certificateMembers filter (_.certificateId === certificateId)
  }


  val oldCertStatesQ = Compiled { certificateId: Rep[Long] =>
    certificateStates filter (_.certificateId === certificateId)
  }

  val oldGoalStatesQ = Compiled { certificateId: Rep[Long] =>
    certificateGoalStates filter (_.certificateId === certificateId)
  }

}
