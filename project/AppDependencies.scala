import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "13.8.0",
    "org.typelevel"     %% "cats-core"                  % "2.13.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % "2.12.0",
    "org.webjars.npm"    % "moment"                     % "2.30.1",
    "uk.gov.hmrc"       %% "tax-year"                   % "5.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup"          % "jsoup"                  % "1.22.2"         % Test,
    "org.scalatestplus" %% "mockito-4-11"           % "3.2.18.0"       % Test
  )
}
