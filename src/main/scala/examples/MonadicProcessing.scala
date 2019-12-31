package examples

import cats._
import cats.implicits._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object MonadicProcessing extends App {

  val sum3Ints: (Int, Int, Int) => Int = _ + _ + _

  {
    println("\n----- processInts for Options using a for comprehension")

    def processInts(
        compute: (Int, Int, Int) => Int
    )(oi1: Option[Int], oi2: Option[Int], oi3: Option[Int]): Option[Int] =
      for {
        i1 <- oi1
        i2 <- oi2
        i3 <- oi3
      } yield compute(i1, i2, i3)

    val result1 = processInts(sum3Ints)(Some(1), Some(2), Some(3))
    println(result1)

    val result2 = processInts(sum3Ints)(Some(1), Some(2), None)
    println(result2)
  }

  {
    println("\n----- processInts for Monad context using a for comprehension")

    def processInts[F[_]: Monad](compute: (Int, Int, Int) => Int)(fi1: F[Int], fi2: F[Int], fi3: F[Int]): F[Int] =
      for {
        i1 <- fi1
        i2 <- fi2
        i3 <- fi3
      } yield compute(i1, i2, i3)

    val result1 = processInts(sum3Ints)(Option(1), Option(2), Option(3))
    println(result1)

    val result2 = processInts(sum3Ints)(Option(1), Option(2), Option.empty[Int])
    println(result2)

    val result3 = processInts(sum3Ints)(List(1, 2), List(10, 20), List(100, 200))
    println(result3)

    val result4 = processInts(sum3Ints)(Future(1), Future(2), Future(3))
    Await.ready(result4, 1.second)
    println(result4)
  }

  {
    println("\n----- processABC for Monad context using a for comprehension")

    def processABC[F[_]: Monad, A, B, C, D](compute: (A, B, C) => D)(fa: F[A], fb: F[B], fc: F[C]): F[D] =
      for {
        a <- fa
        b <- fb
        c <- fc
      } yield compute(a, b, c)

    val result1 = processABC(sum3Ints)(Option(1), Option(2), Option(3))
    println(result1)

    val result2 = processABC(sum3Ints)(Option(1), Option(2), Option.empty[Int])
    println(result2)

    val result3 = processABC(sum3Ints)(List(1, 2), List(10, 20), List(100, 200))
    println(result3)

    val result4 = processABC(sum3Ints)(Future(1), Future(2), Future(3))
    Await.ready(result4, 1.second)
    println(result4)
  }

  {
    println("\n----- processABC for Monad context using flatMap and map")

    def processABC[F[_]: Monad, A, B, C, D](compute: (A, B, C) => D)(fa: F[A], fb: F[B], fc: F[C]): F[D] =
      fa flatMap { a =>
        fb flatMap { b =>
          fc map { c =>
            compute(a, b, c)
          }
        }
      }

    val result1 = processABC(sum3Ints)(Option(1), Option(2), Option(3))
    println(result1)

    val result2 = processABC(sum3Ints)(Option(1), Option(2), Option.empty[Int])
    println(result2)

    val result3 = processABC(sum3Ints)(List(1, 2), List(10, 20), List(100, 200))
    println(result3)

    val result4 = processABC(sum3Ints)(Future(1), Future(2), Future(3))
    Await.ready(result4, 1.second)
    println(result4)

    val result5 =
      processABC(sum3Ints)(Right(1): Either[String, Int], Right(2): Either[String, Int], Right(3): Either[String, Int])
    println(result5)

    val result6 = processABC(sum3Ints)(
      Right(1): Either[String, Int],
      Right(2): Either[String, Int],
      Left("Oooops!"): Either[String, Int]
    )
    println(result6)
  }

  println("\n-----\n")
}
