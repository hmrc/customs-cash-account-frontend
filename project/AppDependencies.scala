import sbt.*

object AppDependencies {

  val bootstrapVersion = "8.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "9.10.0",
    "org.typelevel" %% "cats-core" % "2.10.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.9.0",
    "org.webjars.npm" % "moment" % "2.30.1",
    "uk.gov.hmrc" %% "tax-year" % "4.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.30" % Test
  )
}
