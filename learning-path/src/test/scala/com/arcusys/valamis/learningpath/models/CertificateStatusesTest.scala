package com.arcusys.valamis.learningpath.models

import org.scalatest.FunSuite

/**
  * Created by mminin on 20/01/2017.
  */
class CertificateStatusesTest extends FunSuite {
  test("ids should be like in previous versions") {
    assert(CertificateStatuses.InProgress.id == 0)
    assert(CertificateStatuses.Failed.id == 1)
    assert(CertificateStatuses.Success.id == 2)
    assert(CertificateStatuses.Overdue.id == 3)
  }

  test("names should be like in previous versions") {
    assert("InProgress" == CertificateStatuses.InProgress.toString)
    assert("Failed" == CertificateStatuses.Failed.toString)
    assert("Success" == CertificateStatuses.Success.toString)
    assert("Overdue" == CertificateStatuses.Overdue.toString)
  }
}
