package examples

import scala.language.{higherKinds, postfixOps}

import cats._, cats.implicits._

object WhereFunctorIsTooWeak extends App {

  println("\n----- Where Functor is too weak ...")

  val add1: Int => Int = 1 + _
  val sum2Ints: (Int, Int) => Int = _ + _
  val sum3Ints: (Int, Int, Int) => Int = _ + _ + _

  val res1 = Option(1) map add1
  // oi1: Option[Int] = Some(2)
  println(res1)

  // Option(1) map sum2Ints
  //  [error]  found   : (Int, Int) => Int
  //  [error]  required: Int => ?
  //  [error]   Option(1) map sum2Ints
  //  [error]                 ^

  val res2 = Option(1) map sum2Ints.curried
  // res2: Option[Int => Int] = Some(scala.Function2$$Lambda$4648/454529628@7c9e8cef)
  println(res2)

  // How can I combine two Options to a resulting Option
  // such that the resulting Option contains the sum of the source Options?
  // something like: Option(1) + Option(2) == Option(3)
  //
  // (Option(1), Option(2)) map sum2Ints
  //  [error]  found   : (Int, Int) => Int
  //  [error]  required: Option[Int] => ?
  //  [error]   (Option(1), Option(2)) map sum2Ints
  //  [error]                              ^
  //
  // Applicatives can do that:

  println("----- ... Applicative can help out!")

  val res3 = Option(1) map sum2Ints.curried ap Option(2)
  // res3: Option[Int] = Some(3)
  println(res3)

  val res4 = sum2Ints.curried.pure[Option] ap Option(1) ap Option(2)
  // res4: Option[Int] = Some(3)
  println(res4)

  val res5 = sum2Ints.curried.pure[Option] ap 1.pure[Option] ap 2.pure[Option]
  // res5: Option[Int] = Some(3)
  println(res5)

  val res5a = sum2Ints.curried.pure[Option] <*> 1.pure[Option] <*> 2.pure[Option]
  // res5a: Option[Int] = Some(3)
  println(res5a)

  val res6 = Applicative[Option].ap2(sum2Ints.pure[Option])(Option(1), Option(2))
  // res6: Option[Int] = Some(3)
  println(res6)

  val res7 = Applicative[Option].map2(Option(1), Option(2))(sum2Ints)
  // res7: Option[Int] = Some(3)
  println(res7)

  val res8 = (Option(1), Option(2)) mapN sum2Ints
  // res8: Option[Int] = Some(3)
  println(res8)

  println("----- Cartesian product becomes evident with Applicative[List]")

  val res10 = List((_:Int) * 0, (_:Int) + 100, (x:Int) => x * x) ap List(1, 2, 3)
  // res10: List[Int] = List(0, 0, 0, 101, 102, 103, 1, 4, 9)
  println(res10)

  val add2: Int => Int => Int = ((_:Int) + (_:Int)).curried
  val mult2: Int => Int => Int = ((_:Int) * (_:Int)).curried
  val res11 = List(add2, mult2) <*> List[Int](1, 2) <*> List[Int](100, 200)
  // res11: List[Int] = List(101, 201, 102, 202, 100, 200, 200, 400)
  println(res11)

  val concat: (String, String) => String = _ ++ _
  val res12 = concat.curried.pure[List] <*> List("ha", "heh", "hmm") <*> List("?", "!", ".")
  // res12: List[String] = List(ha?, ha!, ha., heh?, heh!, heh., hmm?, hmm!, hmm.)
  println(res12)

  val res13 = List("ha", "heh", "hmm") map concat.curried ap List("?", "!", ".")
  // res13: List[String] = List(ha?, ha!, ha., heh?, heh!, heh., hmm?, hmm!, hmm.)
  println(res13)

  val res14 = (List("ha", "heh", "hmm"), List("?", "!", ".")) mapN concat
  // res14: List[String] = List(ha?, ha!, ha., heh?, heh!, heh., hmm?, hmm!, hmm.)
  println(res14)

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

  println("-----\n")
}
