import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.4.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.72.0-play-28",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.77.0-play-28",
    "org.typelevel" %% "cats-core" % "1.5.0-RC1",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.50.0",
    "org.webjars.npm" % "moment" % "2.27.0",
    "uk.gov.hmrc" %% "tax-year" % "1.2.0"
  )
  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.4.0" % Test,
    "org.jsoup" % "jsoup" % "1.10.2" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % Test
  )
}
