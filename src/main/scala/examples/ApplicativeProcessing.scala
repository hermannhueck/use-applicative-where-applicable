package examples

import cats._
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{higherKinds, postfixOps}

object ApplicativeProcessing extends App {

  val sum3Ints: (Int, Int, Int) => Int = _ + _ + _

  {
    println("\n----- processInts for Option using Applicative.ap")

    def processInts(compute: (Int, Int, Int) => Int)
                   (oi1: Option[Int], oi2: Option[Int], oi3: Option[Int]): Option[Int] = {
      val fCurried: Int => Int => Int => Int = compute.curried
      val of3: Option[Int => Int => Int => Int] = Some(fCurried)
      val of2: Option[Int => Int => Int] = of3 ap oi1
      val of1: Option[Int => Int] = of2 ap oi2
      val result: Option[Int] = of1 ap oi3
      result
    }

    testWithOptions(processInts(_)(_, _, _))
  }

  {
    println("\n----- processInts for Applicative context using ap and pure")

    def processInts[F[_]: Applicative](compute: (Int, Int, Int) => Int)
                                      (fi1: F[Int], fi2: F[Int], fi3: F[Int]): F[Int] = {
      val fCurried: Int => Int => Int => Int = compute.curried
      val ff: F[Int => Int => Int => Int] = Applicative[F].pure(fCurried)
      ff ap fi1 ap fi2 ap fi3
    }

    testWithOptions(processInts(_)(_, _, _))
    testWithLists(processInts(_)(_, _, _))
    testWithFutures(processInts(_)(_, _, _))
    testWithEithers(processInts(_)(_, _, _))
  }

  {
    println("\n----- processABC for Applicative context using ap and pure")

    def processABC[F[_]: Applicative, A, B, C, D](compute: (A, B, C) => D)
                                                 (fa: F[A], fb: F[B], fc: F[C]): F[D] = {
      val fCurried: A => B => C => D = compute.curried
      val ff: F[A => B => C => D] = Applicative[F].pure(fCurried)
      ff ap fa ap fb ap fc
    }

    testWithOptions(processABC(_)(_, _, _))
    testWithLists(processABC(_)(_, _, _))
    testWithFutures(processABC(_)(_, _, _))
    testWithEithers(processABC(_)(_, _, _))
  }

  {
    println("\n----- processABC for Applicative context using ap / <*> and pure")

    def processABC[F[_]: Applicative, A, B, C, D](compute: (A, B, C) => D)
                                                 (fa: F[A], fb: F[B], fc: F[C]): F[D] =
      compute.curried.pure[F] <*> fa <*> fb <*> fc

    testWithOptions(processABC(_)(_, _, _))
    testWithLists(processABC(_)(_, _, _))
    testWithFutures(processABC(_)(_, _, _))
    testWithEithers(processABC(_)(_, _, _))
  }

  {
    println("\n----- processABC for Applicative context using ap3 and pure")

    def processABC[F[_]: Applicative, A, B, C, D](compute: (A, B, C) => D)
                                                 (fa: F[A], fb: F[B], fc: F[C]): F[D] =
      Applicative[F].ap3(compute.pure[F])(fa, fb, fc)

    testWithOptions(processABC(_)(_, _, _))
    testWithLists(processABC(_)(_, _, _))
    testWithFutures(processABC(_)(_, _, _))
    testWithEithers(processABC(_)(_, _, _))
  }

  {
    println("\n----- processABC for Apply context using Apply.map3")

    def processABC[F[_]: Apply, A, B, C, D](compute: (A, B, C) => D)
                                           (fa: F[A], fb: F[B], fc: F[C]): F[D] =
      Apply[F].map3(fa, fb, fc)(compute)

    testWithOptions(processABC(_)(_, _, _))
    testWithLists(processABC(_)(_, _, _))
    testWithFutures(processABC(_)(_, _, _))
    testWithEithers(processABC(_)(_, _, _))
  }

  {
    println("\n----- processABC for Apply context using mapN")

    def processABC[F[_]: Apply, A, B, C, D](compute: (A, B, C) => D)
                                           (fa: F[A], fb: F[B], fc: F[C]): F[D] =
      (fa, fb, fc) mapN compute

    testWithOptions(processABC(_)(_, _, _))
    testWithLists(processABC(_)(_, _, _))
    testWithFutures(processABC(_)(_, _, _))
    testWithEithers(processABC(_)(_, _, _))
  }

  {
    println("\n----- Partial application of processABC")

    def processABC[F[_]: Apply, A, B, C, D](compute: (A, B, C) => D)
                                           (fa: F[A], fb: F[B], fc: F[C]): F[D] =
      (fa, fb, fc) mapN compute

    // We first apply the value of the compute function.
    // This gives us a def with type parameter F[_] for the yet unspecified effect
    // A val cannot have a type parameter.
    def processEffectfulInts[F[_]: Apply](fi1: F[Int], fi2: F[Int], fi3: F[Int]): F[Int] =
      processABC(sum3Ints)(fi1, fi2, fi3)

    val result1 = processEffectfulInts(Option(1), Option(2), Option(3))
    println(result1)
    val result2 = processEffectfulInts(Option(1), Option(2), Option.empty[Int])
    println(result2)
    val result3 = processEffectfulInts(List(1, 2), List(10, 20), List(100, 200))
    println(result3)


    type Opts2Opt[A] = (Option[A], Option[A], Option[A]) => Option[A]
    type Lists2List[A] = (List[A], List[A], List[A]) => List[A]

    val processOptionInts: Opts2Opt[Int] = processEffectfulInts[Option]
    val processListInts: Lists2List[Int] = processEffectfulInts[List]

    // Now we apply a concrete type constructor for F[_]: Option, List, Future etc.
    // That gives us a val which we can pass aroung to other functions.
    def invoke(processOptionInts: Opts2Opt[Int], processListInts: Lists2List[Int]): Unit = {

      val result1 = processOptionInts(Option(1), Option(2), Option(3))
      println(result1)
      val result2 = processOptionInts(Option(1), Option(2), Option.empty[Int])
      println(result2)
      val result3 = processListInts(List(1, 2), List(10, 20), List(100, 200))
      println(result3)
    }

    invoke(processOptionInts, processListInts)
  }

  private def testWithOptions(processOptionInts: ((Int, Int, Int) => Int, Option[Int], Option[Int], Option[Int]) => Option[Int]): Unit = {

    val result1 = processOptionInts(sum3Ints, Option(1), Option(2), Option(3))
    println(result1)

    val result2 = processOptionInts(sum3Ints, Option(1), Option(2), Option.empty[Int])
    println(result2)
  }

  private def testWithLists(processListInts: ((Int, Int, Int) => Int, List[Int], List[Int], List[Int]) => List[Int]): Unit = {

    val result = processListInts(sum3Ints, List(1, 2), List(10, 20), List(100, 200))
    println(result)
  }

  private def testWithFutures(processFutureInts: ((Int, Int, Int) => Int, Future[Int], Future[Int], Future[Int]) => Future[Int]): Unit = {

    val result = processFutureInts(sum3Ints, Future(1), Future(2), Future(3))
    Await.ready(result, 1 second)
    println(result)
  }

  private def testWithEithers(processEitherInts: ((Int, Int, Int) => Int, Either[String, Int], Either[String, Int], Either[String, Int]) => Either[String, Int]): Unit = {

    val result1 = processEitherInts(sum3Ints, Right(1): Either[String, Int], Right(2): Either[String, Int], Right(3): Either[String, Int])
    println(result1)

    val result2 = processEitherInts(sum3Ints, Right(1): Either[String, Int], Right(2): Either[String, Int], Left("Oooops!"): Either[String, Int])
    println(result2)
  }

  println("\n-----\n")
}
