/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.email

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class EmailResponsesSpec extends SpecBase {

  "UndeliverableInformationEvent" must {

    "generate correct output" when {

      "performing Reads" in new Setup {

        import UndeliverableInformationEvent.format

        Json.fromJson(Json.parse(sampleResponseForUndeliverableEvent)) mustBe JsSuccess(undelInfoEventOb)
      }

      "performing Writes" in new Setup {
        Json.toJson(undelInfoEventOb) mustBe Json.parse(sampleResponseForUndeliverableEvent)
      }
    }
  }

  "UndeliverableInformation" must {

    "generate correct output" when {

      "performing Reads" in new Setup {

        import UndeliverableInformation.format

        Json.fromJson(Json.parse(sampleResponseForUndeliverableInfo)) mustBe JsSuccess(undelInfoOb)
      }

      "performing Writes" in new Setup {
        Json.toJson(undelInfoOb) mustBe Json.parse(sampleResponseForUndeliverableInfo)
      }
    }
  }

  "EmailResponse" must {

    "generate correct output" when {

      "performing Reads" in new Setup {

        import EmailResponse.format

        Json.fromJson(Json.parse(sampleEmailResponse)) mustBe JsSuccess(emailResponseOb)
      }

      "performing Writes" in new Setup {
        Json.toJson(emailResponseOb) mustBe Json.parse(sampleEmailResponse)
      }
    }
  }

  "EmailVerifiedResponse.Reads" must {

    "generate correct EmailVerifiedResponse object" in new Setup {

      import EmailVerifiedResponse.format

      Json.fromJson(Json.parse(emailVerifiedResponse)) mustBe JsSuccess(emailVerifiedResponseOb)
    }
  }

  "EmailVerifiedResponse.Writes" must {

    "generate correct JsValue for EmailVerifiedResponse object" in new Setup {
      Json.toJson(emailVerifiedResponseOb) mustBe Json.parse(emailVerifiedResponse)
    }
  }

  "EmailUnverifiedResponse.Reads" must {

    "generate correct EmailUnverifiedResponse object" in new Setup {

      import EmailUnverifiedResponse.format

      Json.fromJson(Json.parse(emailUnverifiedResponse)) mustBe JsSuccess(emailUnverifiedResponseOb)
    }
  }

  "EmailUnverifiedResponse.Writes" must {

    "generate correct JsValue for EmailUnverifiedResponse object" in new Setup {
      Json.toJson(emailUnverifiedResponseOb) mustBe Json.parse(emailUnverifiedResponse)
    }
  }

  trait Setup {
    val sampleResponseForUndeliverableInfo: String =
      """{
        |    "subject": "subject-example",
        |    "eventId": "example-id",
        |    "groupId": "example-group-id",
        |    "timestamp": "2021-05-14T10:59:45.811+01:00",
        |    "event": {
        |      "id": "example-id",
        |      "event": "someEvent",
        |      "emailAddress": "email@email.com",
        |      "detected": "2021-05-14T10:59:45.811+01:00",
        |      "code": 12,
        |      "reason": "Inbox full",
        |      "enrolment": "HMRC-CUS-ORG~EORINumber~GB744638982004"
        |    }
        |  }""".stripMargin

    val sampleResponseForUndeliverableEvent: String =
      """{
        |  "id": "example-id",
        |  "event": "someEvent",
        |  "emailAddress": "email@email.com",
        |  "detected": "2021-05-14T10:59:45.811+01:00",
        |  "code": 12,
        |  "reason": "Inbox full",
        |  "enrolment": "HMRC-CUS-ORG~EORINumber~GB744638982004"
        |}""".stripMargin

    val sampleEmailResponse: String =
      """{
        |  "address": "john.doe@example.com",
        |  "timestamp": "2023-12-15T23:25:25.000Z",
        |  "undeliverable": {
        |    "subject": "subject-example",
        |    "eventId": "example-id",
        |    "groupId": "example-group-id",
        |    "timestamp": "2021-05-14T10:59:45.811+01:00",
        |    "event": {
        |      "id": "example-id",
        |      "event": "someEvent",
        |      "emailAddress": "email@email.com",
        |      "detected": "2021-05-14T10:59:45.811+01:00",
        |      "code": 12,
        |      "reason": "Inbox full",
        |      "enrolment": "HMRC-CUS-ORG~EORINumber~GB744638982004"
        |    }
        |  }
        |}""".stripMargin

    val value = 12
    val undelInfoEventOb: UndeliverableInformationEvent = UndeliverableInformationEvent("example-id",
      "someEvent",
      "email@email.com",
      "2021-05-14T10:59:45.811+01:00",
      Some(value),
      Some("Inbox full"),
      "HMRC-CUS-ORG~EORINumber~GB744638982004")

    val undelInfoOb: UndeliverableInformation = UndeliverableInformation("subject-example",
      "example-id",
      "example-group-id",
      "2021-05-14T10:59:45.811+01:00",
      undelInfoEventOb)

    val emailResponseOb: EmailResponse = EmailResponse(address = Some("john.doe@example.com"),
      timestamp = Some("2023-12-15T23:25:25.000Z"),
      undeliverable = Some(undelInfoOb))

    val emailAddress = "someemail@mail.com"

    val emailUnverifiedResponse: String = """{"unVerifiedEmail":"someemail@mail.com"}""".stripMargin

    val emailUnverifiedResponseOb: EmailUnverifiedResponse =
      EmailUnverifiedResponse(unVerifiedEmail = Some(emailAddress))

    val emailVerifiedResponse: String = """{"verifiedEmail":"someemail@mail.com"}""".stripMargin

    val emailVerifiedResponseOb: EmailVerifiedResponse =
      EmailVerifiedResponse(verifiedEmail = Some(emailAddress))
  }
}
