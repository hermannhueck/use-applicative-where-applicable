package examples

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Traversals extends App {

  println("\n===== Traversals")

  {
    println("----- Future.sequence")

    val lfd: List[Future[Double]] = List(Future(2.0), Future(4.0), Future(6.0))
    val fld: Future[List[Double]] = Future.sequence(lfd)
    Await.ready(fld, 1.second)
    println(fld)
  }

  {
    println("----- List#map + Future.sequence")

    val li: List[Int]                        = List(1, 2, 3)
    val doubleItAsync: Int => Future[Double] = x => Future { x * 2.0 }
    val lfi: List[Future[Double]]            = li.map(doubleItAsync)
    val fld: Future[List[Double]]            = Future.sequence(lfi)
    Await.ready(fld, 1.second)
    println(fld)
  }

  {
    println("----- Future.traverse")

    val li: List[Int]                        = List(1, 2, 3)
    val doubleItAsync: Int => Future[Double] = x => Future { x * 2.0 }
    val fld: Future[List[Double]]            = Future.traverse(li)(doubleItAsync)
    Await.ready(fld, 1.second)
    println(fld)
  }

  {
    println("----- Future.traverse with identity function")

    val lfd: List[Future[Double]] = List(Future(2.0), Future(4.0), Future(6.0))
    val fld: Future[List[Double]] = Future.traverse(lfd)(identity)
    Await.ready(fld, 1.second)
    println(fld)
  }

  {
    println("----- Traverse#sequence for List and Future")

    import cats.Traverse
    import cats.instances.list._
    import cats.instances.future._

    val lfd: List[Future[Double]] = List(Future(2.0), Future(4.0), Future(6.0))

    val fld1: Future[List[Double]] = Traverse[List].sequence(lfd)
    Await.ready(fld1, 1.second)
    println(fld1)

    import cats.syntax.traverse._ // supports 'sequence' as an enrichment of List

    val fld2: Future[List[Double]] = lfd.sequence
    Await.ready(fld2, 1.second)
    println(fld2)
  }

  {
    println("----- Traverse#traverse for List and Future")

    import cats.Traverse
    import cats.instances.list._
    import cats.instances.future._

    val li: List[Int]                        = List(1, 2, 3)
    val doubleItAsync: Int => Future[Double] = x => Future { x * 2.0 }

    val fld1: Future[List[Double]] = Traverse[List].traverse(li)(doubleItAsync)
    Await.ready(fld1, 1.second)
    println(fld1)

    import cats.syntax.traverse._ // supports 'traverse' as an enrichment of List

    val fld2: Future[List[Double]] = li traverse doubleItAsync
    Await.ready(fld2, 1.second)
    println(fld2)
  }

  {
    println("----- Traverse#traverse for Vector and Option")

    import cats.Traverse
    import cats.instances.vector._
    import cats.instances.option._

    val vi1: Vector[Int] = Vector(3, 2, 1)
    val vi2: Vector[Int] = Vector(3, 2, 0)

    val divideBy: Int => Option[Double] = x => if (x == 0) None else Some { 6.0 / x }

    val ovd1_1: Option[Vector[Double]] = Traverse[Vector].traverse(vi1)(divideBy)
    println(ovd1_1)
    val ovd1_2: Option[Vector[Double]] = Traverse[Vector].traverse(vi2)(divideBy)
    println(ovd1_2)

    import cats.syntax.traverse._ // supports 'traverse' as an enrichment of Vector

    val ovd2_1: Option[Vector[Double]] = vi1 traverse divideBy
    println(ovd2_1)
    val ovd2_2: Option[Vector[Double]] = vi2 traverse divideBy
    println(ovd2_2)
  }

  println("\n-----\n")
}
