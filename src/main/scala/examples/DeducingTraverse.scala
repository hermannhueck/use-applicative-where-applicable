package examples

import cats._
import cats.implicits._

import scala.language.{higherKinds, postfixOps}

object DeducingTraverse extends App {

  println("\n----- Deducing Traverse ...")

  println("----- Manipulating the List inside Option[List[Int]]")

  val res15 = Option(3) map {head => (tail: List[Int]) => head :: tail} ap Option(List(4, 4, 4))
  // res15: Option[List[Int]] = Some(List(3, 4, 4, 4))
  println(res15)

  val res16 = Applicative[Option].map2(Option(3), Option(List(4, 4, 4)))(_ :: _)
  // res16: Option[List[Int]] = Some(List(3, 4, 4, 4))
  println(res16)

  val res17 = (Option(3), Option(List(4, 4, 4))) mapN (_ :: _)
  // res17: Option[List[Int]] = Some(List(3, 4, 4, 4))
  println(res17)

  println("----- listSequencing with Applicatives")

  def listSequence1[F[_]: Applicative, A](lfa: List[F[A]]): F[List[A]] = lfa match {
    case Nil => List.empty[A].pure[F]
    case x :: xs => (x, listSequence1(xs)) mapN (_ :: _)
  }

  def listSequence2[F[_]: Applicative, A](lfa: List[F[A]]): F[List[A]] =
    lfa.foldRight(List.empty[A].pure[F])((x, xs) => (x, xs) mapN (_ :: _))

  // List[Option[A]] => Option[List[A]]

  val res18 = listSequence1(List(Option("---"), Option("List[Maybe[A]]"), Option("becomes"), Option("Maybe[List[A]]"), Option("---")))
  // res18: Option[List[String]] = Some(List(---, List[Maybe[A]], becomes, Maybe[List[A]], ---))
  println(res18)

  val res19 = listSequence2(List(Option("---"), Option("List[Maybe[A]]"), Option("becomes"), Option("Maybe[List[A]]"), Option("---")))
  // res19: Option[List[String]] = Some(List(---, List[Maybe[A]], becomes, Maybe[List[A]], ---))
  println(res19)

  val res20 = listSequence2(List(Option("---"), Option("List[Maybe[A]]"), Option("becomes"), Option("Maybe[List[A]]"), Option.empty))
  // res20: Option[List[String]] = None
  println(res20)

  // List[List[A]] => List[List[A]]

  val res21 = listSequence2(List(List(1,2,3), List(10, 20, 30)))
  // res21: List[List[Int]] = List(List(1, 10), List(1, 20), List(1, 30), List(2, 10), List(2, 20), List(2, 30), List(3, 10), List(3, 20), List(3, 30))
  println(res21)

  val res22 = listSequence2(List(List(1,2,3), List(10, 20, 30), List.empty))
  // res22: List[List[Int]] = List()
  println(res22)

  // List[Function1[A]] => Function1[List[A]]

  val fI2LI: Int => List[Int] = listSequence2(List[Int => Int](_ + 1, _ + 2, _ + 3))
  // fI2LI: Int => List[Int] = scala.Function1$$Lambda$596/926905424@8b9f81b
  val res23: List[Int] = fI2LI(10)
  // res23: List[Int] = List(11, 12, 13)
  println(res23)

  println("----- listTraversing with Applicatives")

  def listTraverse1[F[_]: Applicative, A, B](la: List[A])(f: A => F[B]): F[List[B]] = la match {
    case Nil => List.empty[B].pure[F]
    case x :: xs => (f(x), listTraverse1(xs)(f)) mapN (_ :: _)
  }

  def listTraverse2[F[_]: Applicative, A, B](la: List[A])(f: A => F[B]): F[List[B]] =
    la.foldRight(List.empty[B].pure[F])((x, xs) => (f(x), xs) mapN (_ :: _))

  // List[A] => Option[List[B]]

  val res24 = listTraverse1[Option, String, String](List("---", "List[Maybe[A]]", "becomes", "Maybe[List[A]]", "---"))(s => Option(s.toUpperCase))
  // res24: Option[List[String]] = Some(List(---, LIST[MAYBE[A]], BECOMES, MAYBE[LIST[A]], ---))
  println(res24)

  val res25 = listTraverse2[Option, String, String](List("---", "List[Maybe[A]]", "becomes", "Maybe[List[A]]", "---"))(s => Option(s.toUpperCase))
  // res25: Option[List[String]] = Some(List(---, LIST[MAYBE[A]], BECOMES, MAYBE[LIST[A]], ---))
  println(res25)

  val res26 = listTraverse2[Option, String, String](List("---", "List[Maybe[A]]", "becomes", "Maybe[List[A]]", "---"))(
                                                    (s: String) => if (s.length % 2 != 0) Option.empty else Option(s))
  // res26: Option[List[String]] = None
  println(res26)

  // List[A] => List[List[B]]

  val res27: List[List[Double]] = listTraverse2[List, Int, Double](List(1,2))(x => List(x+10.0, x+20.0))
  // res27: List[List[Double]] = List(List(11.0, 12.0), List(11.0, 22.0), List(21.0, 12.0), List(21.0, 22.0))
  println(res27)

/*
  // List[Function1[A]] => Function1[List[A]]

  val fI2LI2: Int => List[Int] = listTraverse2[Function1[Int, ?], Int, Int](List(1,2,3))((x:Int) => List(x))
  // fI2LI: Int => List[Int] = scala.Function1$$Lambda$596/926905424@8b9f81b
  println(fI2LI2)
  //val res28: List[Int] = fI2LI2(10)
  // res28: List[Int] = List(11, 12, 13)
  //println(res28)

*/
  println("-----\n")
}
