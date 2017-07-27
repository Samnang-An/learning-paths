package com.arcusys.valamis.learningpath.utils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Created by pkornilov on 6/20/17.
  */
trait FutureHelpers {

  protected def await[R](f: Future[R]): R = {
    Await.result(f, Duration.Inf)
  }

}
