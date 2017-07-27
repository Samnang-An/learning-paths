package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.PackageGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait PackageGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import profile.api._

  class PackageGoalTable(tag: Tag)
    extends Table[PackageGoal](tag, "LEARN_CERT_GOALS_PACKAGE")
      with CertificateGoalBaseColumns {

    def packageId = column[Long]("PACKAGE_ID")

    def * = (goalId, certificateId, packageId) <> (PackageGoal.tupled, PackageGoal.unapply)

  }

  val packageGoals = TableQuery[PackageGoalTable]
}