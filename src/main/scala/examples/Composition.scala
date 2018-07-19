package examples

import cats.Applicative
import cats.data.OptionT
import cats.instances.list._
import cats.instances.option._

import scala.language.{higherKinds, postfixOps}

object Composition extends App {

  println("\n----- Monadic and Applicative composition")

  val loi1 = List(Some(1), Some(2))
  val loi2 = List(Some(10), Some(20))

  def processMonadic(xs: List[Option[Int]], ys: List[Option[Int]]): List[Option[Int]] = {
    val otli: OptionT[List, Int] =
      for {
        x <- OptionT[List, Int](xs)
        y <- OptionT[List, Int](ys)
      } yield x + y
    otli.value
  }

  val result1 = processMonadic(loi1, loi2)
  println(result1)

  def processApplicative(loi1: List[Option[Int]], loi2: List[Option[Int]]): List[Option[Int]] =
    Applicative[List].compose[Option].map2(loi1, loi2)((_:Int) + (_:Int))

  val result2 = processApplicative(loi1, loi2)
  println(result2)

  println("\n-----\n")
}
