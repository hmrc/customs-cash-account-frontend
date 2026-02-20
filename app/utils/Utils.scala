/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import config.AppConfig
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTable
import views.html.components.{dl, h1, h1Inner, h2, h2Inner, link, newTabLink, notification_panel, p}
import uk.gov.hmrc.hmrcfrontend.views.html.components.HmrcNewTabLink
import uk.gov.hmrc.hmrcfrontend.views.html.helpers.HmrcNewTabLinkHelper

object Utils {
  val comma: String       = ","
  val emptyString: String = ""
  val period: String      = "."
  val hyphen              = "-"
  val singleSpace         = " "
  val negativeSign        = "-"
  val poundSymbol         = "£"

  val emptyH1Component: h1                            = new h1()
  val emptyH2Component: h2                            = new h2()
  val emptyPComponent: p                              = new p()
  val emptyLinkComponent: link                        = new link()
  val emptyHmrcNewTabLink: HmrcNewTabLink             = new HmrcNewTabLink()
  val emptyHmrcNewTabLinkHelper: HmrcNewTabLinkHelper = new HmrcNewTabLinkHelper(emptyHmrcNewTabLink)
  val emptyGovUkTableComponent                        = new GovukTable()
  val emptyH2InnerComponent                           = new h2Inner()
  val emptyH1InnerComponent                           = new h1Inner()
  val emptyDlComponent                                = new dl()

  def dlComponent(dtMsg: String, id: Option[String] = None, classes: String = "govuk-caption-xl")(
    implicit messages: Messages
  ): HtmlFormat.Appendable =
    new dl().apply(dtMsg = dtMsg, id = id, classes = classes)

  def h2Component(
    msgKey: String,
    id: Option[String] = None,
    classes: String = "govuk-heading-m",
    extraContent: Option[String] = None
  )(implicit messages: Messages): HtmlFormat.Appendable =
    new h2().apply(msg = msgKey, id = id, classes = classes, extraContent = extraContent)

  def buildCacheId(accountNumber: String, searchValue: String): String =
    s"${accountNumber}_$searchValue"

  def extractNumericValue(amount: String): String =
    amount.replaceAll("[^\\d.-]", emptyString)

  def h2InnerComponent(
    msgKey: String,
    id: Option[String] = None,
    innerMsg: String,
    classes: String = "govuk-caption-xl"
  )(implicit messages: Messages): HtmlFormat.Appendable =
    new h2Inner().apply(msg = msgKey, id = id, classes = classes, innerMsg = innerMsg)

  def notificationPanelComponent(
    showNotification: Boolean,
    preMessage: String,
    linkUrl: String,
    linkText: String,
    postMessage: String
  )(implicit messages: Messages): HtmlFormat.Appendable =
    new notification_panel().apply(
      showNotification = showNotification,
      preMessage = preMessage,
      linkUrl = linkUrl,
      linkText = linkText,
      postMessage = postMessage
    )

  def linkComponent(input: LinkComponentValues)(implicit messages: Messages): HtmlFormat.Appendable =
    new link().apply(
      linkMessageKey = input.linkMessageKey,
      location = input.location,
      linkId = input.linkId,
      linkClass = input.linkClass,
      pWrapped = input.pWrapped,
      linkSentence = input.linkSentence,
      preLinkMessage = input.preLinkMessageKey,
      postLinkMessage = input.postLinkMessageKey,
      pId = input.pId,
      pClass = input.pClass,
      linkMessage = input.linkMessage,
      enableLineBreakBeforePostMessage = input.enableLineBreakBeforePostMessage
    )

  def pComponent(messageKey: String, classes: String = "govuk-body", id: Option[String] = None, bold: Boolean = false)(
    implicit messages: Messages
  ): HtmlFormat.Appendable =
    new p().apply(message = messageKey, classes = classes, id = id, bold = bold)

  def hmrcNewTabLinkComponent(
    linkMessage: String,
    href: String,
    preLinkMessage: Option[String] = None,
    postLinkMessage: Option[String] = None,
    classes: String = "govuk-body"
  )(implicit messages: Messages, config: AppConfig): HtmlFormat.Appendable =
    new newTabLink(emptyHmrcNewTabLink, emptyHmrcNewTabLinkHelper).apply(
      linkMessage = linkMessage,
      href = href,
      preLinkMessage = preLinkMessage,
      postLinkMessage = postLinkMessage,
      classes = classes
    )

  def prependNegativeSignWithAmount(amount: String): String =
    s"$negativeSign$amount"

  case class LinkComponentValues(
    linkMessageKey: String = emptyString,
    location: String,
    linkId: Option[String] = None,
    linkClass: String = "govuk-link",
    pWrapped: Boolean = true,
    linkSentence: Boolean = false,
    preLinkMessageKey: Option[String] = None,
    postLinkMessageKey: Option[String] = None,
    pId: Option[String] = None,
    pClass: String = "govuk-body",
    linkMessage: Option[String] = None,
    enableLineBreakBeforePostMessage: Boolean = false
  )

  case class DetailsHint(
    summaryText: String,
    text: String,
    classes: String = emptyString,
    attributes: Map[String, String] = Map.empty,
    open: Boolean = false
  )

  case class LabelHint(labelText: String, classes: String = emptyString)

  case class InputTextHint(detailsHint: Option[DetailsHint] = None, labelHint: Option[LabelHint] = None)
}
