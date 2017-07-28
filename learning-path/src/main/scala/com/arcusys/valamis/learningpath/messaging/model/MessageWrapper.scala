package com.arcusys.valamis.learningpath.messaging.model

/**
  * Created by pkornilov on 6/16/17.
  */
trait MessageWrapper[T] {
  def getOriginalMessage: T

  def getFieldOptional(name: String): Option[AnyRef]

  def getFieldWithCheck(name: String): AnyRef = {
    getFieldOptional(name) getOrElse {
      throw new NoSuchElementException(s"no $name field")
    }
  }

  def getStringWithCheck(name: String): String = {
    getFieldWithCheck(name: String).toString
  }

  def getLongWithCheck(name: String): Long = {
    getStringWithCheck(name).toLong
  }
}
