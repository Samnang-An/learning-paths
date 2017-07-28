package com.arcusys.valamis.learningpath.listeners.competences

import com.arcusys.valamis.learningpath.listeners.competences.messages.ItemChangedMessage
import com.arcusys.valamis.learningpath.models.CompetenceMessageActions._
import com.arcusys.valamis.learningpath.services.CompetenceService
import com.arcusys.valamis.learningpath.utils.{FutureHelpers, JsonHelper}
import com.arcusys.valamis.message.broker.MessageListener

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 6/15/17.
  */
class SkillChangeListener(val recommendedCompetenceService: CompetenceService,
                          val improvingCompetenceService: CompetenceService)
                         (implicit val executionContext: ExecutionContext) extends MessageListener
  with FutureHelpers {

  import JsonHelper.formats

  override def processMessage(data: String)
                             (implicit companyId: Long): Option[String] = {
    val msg = JsonHelper.fromJson(data).extract[ItemChangedMessage]
    msg.action match {
      case SkillChanged | SkillDeleted =>
        //if skill is deleted, it's not deleted from LPs, but we still update its name
        //just in case it wasn't updated before
        await {
          for {
            _ <- improvingCompetenceService.updateSkillName(msg.id, msg.name)
            _ <- recommendedCompetenceService.updateSkillName(msg.id, msg.name)
          } yield None
        }

      case _ => throw new NoSuchMethodException(s"Action ${msg.action} is not supported")
    }
  }

}
