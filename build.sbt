name := "use-applicative-where-applicable"
version := "0.1.0"

scalaVersion := "2.13.1"

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
  "org.typelevel"  %% "cats-effect" % "2.1.2",
  "org.scalatest"  %% "scalatest"   % "3.1.1" % Test,
  "org.scalacheck" %% "scalacheck"  % "1.14.3" % Test,
  compilerPlugin("org.typelevel" % "kind-projector"      % "0.11.0" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

initialCommands in console :=
  """import scala.language.{higherKinds, postfixOps}
    |
    |import cats._
    |import cats.implicits._
    |
    |import scala.concurrent.{Await, Future}
    |import scala.concurrent.duration._
    |import scala.concurrent.ExecutionContext.Implicits.global
    |""".stripMargin
