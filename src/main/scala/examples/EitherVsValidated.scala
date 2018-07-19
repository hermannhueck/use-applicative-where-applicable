package examples

/*
import cats.Applicative
import cats.Semigroupal
import cats.instances.either._
import cats.syntax.validated._
import cats.instances.string._ // for Semigroup
*/

import scala.language.{higherKinds, postfixOps}

object EitherVsValidated extends App {

  println("\n===== Either vs. Validated")

  {
    println("\n----- Monadic processing with Either: 2nd error is lost!")

    import cats.syntax.either._

    val result: Either[List[String], Int] = for {
      x <- 5.asRight[List[String]]
      y <- List("Error 1").asLeft[Int]
      z <- List("Error 2").asLeft[Int] // List("Error 2") is lost!
    } yield x + y + z

    println(result)
  }

  {
    println("\n----- Applicative processing with Either: 2nd error is lost!")

    import cats.instances.either._
    import cats.syntax.either._
    import cats.syntax.apply._

    val result: Either[List[String], Int] =
      (5.asRight[List[String]],
        List("Error 1").asLeft[Int],
        List("Error 2").asLeft[Int] // List("Error 2") is lost!
      ) mapN ((_: Int) + (_: Int) + (_: Int))

    println(result)
  }

  {
    println("\n----- Applicative processing with Validated: 2nd error is preserved!")

    import cats.data.Validated
    import cats.syntax.validated._
    import cats.syntax.apply._
    import cats.instances.list._
    import cats.instances.string._

    val result: Validated[List[String], Int] =
      (5.valid[List[String]],
        List("Error 1").invalid[Int],
        List("Error 2").invalid[Int] // List("Error 2") is preserved!
      ) mapN ((_: Int) + (_: Int) + (_: Int))

    println(result)
  }

  {
    println("\n----- Conversions between Either and Validated")

    import cats.data.Validated
    import cats.syntax.validated._
    import cats.syntax.either._
    import cats.syntax.apply._
    import cats.instances.list._
    import cats.instances.string._

    val result: Validated[List[String], Int] =
      (5.asRight[List[String]].toValidated,
        List("Error 1").asLeft[Int].toValidated,
        List("Error 2").asLeft[Int].toValidated // List("Error 2") is preserved!
      ) mapN ((_: Int) + (_: Int) + (_: Int))

    val resultAsEither = result.toEither
    println(resultAsEither)
  }

  println("\n-----\n")
}
