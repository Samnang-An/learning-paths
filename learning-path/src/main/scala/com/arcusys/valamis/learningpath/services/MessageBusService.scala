package com.arcusys.valamis.learningpath.services

import scala.util.Try


/**
  * Created by pkornilov on 3/10/17.
  */
trait MessageBusService {
  def sendSynchronousMessage(destinationName: String, data: java.util.HashMap[String, AnyRef]): Try[Object]
  def sendAsynchronousMessage(destinationName: String, data: java.util.HashMap[String, AnyRef]): Unit

}
