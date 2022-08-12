name := "use-applicative-where-applicable"
version := "0.1.0"

scalaVersion := "2.13.8"

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",           // source files are in UTF-8
  "-deprecation",    // warn about use of deprecated APIs
  "-unchecked",      // warn about unchecked type parameters
  "-feature",        // warn about misused language features
  "-Xlint",          // enable handy linter warnings
  "-Xfatal-warnings" // turn compiler warnings into errors
)

libraryDependencies ++= Seq(
  "org.typelevel"  %% "cats-effect" % "3.3.14",
  "org.scalatest"  %% "scalatest"   % "3.2.13" % Test,
  "org.scalacheck" %% "scalacheck"  % "1.16.0" % Test,
  compilerPlugin("org.typelevel" % "kind-projector"      % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

console / initialCommands :=
  """import scala.language.{higherKinds, postfixOps}
    |
    |import cats._
    |import cats.implicits._
    |
    |import scala.concurrent.{Await, Future}
    |import scala.concurrent.duration._
    |import scala.concurrent.ExecutionContext.Implicits.global
    |""".stripMargin
