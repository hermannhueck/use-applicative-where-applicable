name := "use-applicative-where-applicable"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",   // source files are in UTF-8
  "-deprecation",         // warn about use of deprecated APIs
  "-unchecked",           // warn about unchecked type parameters
  "-feature",             // warn about misused language features
  //"-Xlint",               // enable handy linter warnings
  "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
  //"-language:higherKinds",// allow higher kinded types without `import scala.language.higherKinds`
  // "-Xfatal-warnings",     // turn compiler warnings into errors
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

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.1.0",
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
