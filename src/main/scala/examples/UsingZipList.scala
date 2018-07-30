package examples

import cats._
import cats.data._
import cats.implicits._

import scala.language.{higherKinds, postfixOps}

object UsingZipList extends App {

  println("\n----- Using ZipList ...")

  val li = List(1, 2, 3)
  val lf: List[Int => Int] = List(_ + 10, _ + 20)

  val zli: ZipList[Int] = ZipList(li)
  val zlf: ZipList[Int => Int] = ZipList(lf)

  println("----- Functor ZipList: using map")

  println(  zli.map(_ + 10).value  )

  println("----- Applicative ZipList: using ap")

  println(  lf ap li  )
  println(  (zlf ap zli).value  )
  println(  (zli, zli).mapN(_ + _).value  )
  println(  (zli, zli).mapN((_, _)).value  )
  println(  (zli, zli).tupled.value  )
  println(  (zli, zli, zli).mapN(_ + _ + _).value  )
  println(  (zli, zli, zli).mapN((_, _, _)).value  )
  println(  (zli, zli, zli).tupled.value  )

  println("----- Monad ZipList??? No flatMap available for ZipList!")

  println("-----\n")
}
