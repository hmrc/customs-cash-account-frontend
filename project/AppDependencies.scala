import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.19.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.14.0-play-28",
    "org.typelevel" %% "cats-core" % "2.9.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "org.webjars.npm" % "moment" % "2.29.4",
    "uk.gov.hmrc" %% "tax-year" % "3.2.0"
  )
  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.19.0" % Test,
    "org.jsoup" % "jsoup" % "1.16.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % Test
  )
}
