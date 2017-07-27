package com.arcusys.valamis.learningpath.web.servlets.utils

import org.json4s.{Formats, KeySerializer, TypeInfo}

import scala.reflect.ClassTag

class EnumNameKeySerializer[E <: Enumeration : ClassTag](enum: E)
  extends KeySerializer[E#Value] {


  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, String), E#Value] = {
    case (t@TypeInfo(EnumerationClass, _), value) if isValid(value) => {
      enum.withName(value)
    }
  }

  private[this] def isValid(value: String) = {
    enum.values.exists(_.toString == value)
  }

  def serialize(implicit format: Formats): PartialFunction[Any, String] = {
    case i: E#Value => i.toString
  }
}
