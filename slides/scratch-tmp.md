
name: main-template
layout: true
class: left, top, inverse

---

class: center

<br/>
<br/>
# Use Applicative
# where applicable!
<br/>
<br/>
<br/>
## &copy; 2018 Hermann Hueck
<br/>
https://github.com/hermannhueck/use-applicative-where-applicable

---

# Abstract
<br/>
<br/>
In this talk I will explain what Free Monads are and how to use them (using the Cats implementation).

After having shown the basics I build a small app by composing several
Free Monads to a small program.

I discuss the pros and cons of this technique.

Finally I will demonstrate how to avoid some boilerplate with the *FreeK* library.

---

# Agenda
<br/>

1. Free Monads - what they are.
2. Free Monad Recipe
3. Extending your DSL
4. *FunctionK* and Natural Transformation
5. More Interpreters
6. Partial Lifting with *Free.inject*
7. Composing two DSLs and two Interpreters
8. Composing three DSLs and three Interpreters
9. Routing the workflow through DSLs
10. Pros &amp; Cons
11. The *FreeK* library
12. Resources

---

class: middle, center

# 1. Free Monad - What is it?
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

---

## Free Monad - What is it?
<br/>

A free monad is a construction which allows you to build a monad from any ADT with a type parameter.
Like other monads, it is a pure way to represent and manipulate computations.

In particular, free monads provide a practical way to:

- represent stateful computations as data, and run them
- run recursive computations in a stack-safe way
- build an embedded DSL (domain-specific language)
- retarget a computation to another interpreter using natural transformations

(https://typelevel.org/cats/datatypes/freemonad.html)

---

class: middle, center

# 2. Free Monad Recipe
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app1.MyApp*
]

---

## Recipe, how to proceed ...
<br/>

1. Study your topic/domain/use case. Which operations do you need?
2. Create an ADT (algebraic data type) for the operations (computions as data)
3. "Lift" your ADT into the Free Monad, i.e. use the Free Monad to implement a smart constructor (lowercased function) for each element of your ADT. This is your DSL.
4. Write one or more interpreters for your DSL (using natural transformation)
5. Build a program using the DSL (typically a for comprehension). The program is not executable.
6. Execute the program with one of your interpreters.

---

## Step 1: Study your topic / domain / use case.
<br/>

Which operations do you need?

In this very simple exmaple we want to interact with a user at a terminal.
We want to print an output string to the terminal or get a string of user input from the terminal.

Operations:

- PRINT LINE: displays an output string to the user and returns nothing
- GET LINE: returns an input string entered by the user

---

## Step 2: Create an ADT for the operations
<br/>

```scala
// Algebra as an ADT - one type param for the return type
trait Inout[A]
final case class Printline(out: String) extends Inout[Unit]
case object Getline extends Inout[String]
````

<u>Note:</u> The trait's type parameter represents the return type of an operation.<br/>
The *Printline* operation returns nothing, hence it extends *Inout[Unit]*.<br/>
The *Getline* operation returns a String, hence it extends *Inout[String]*.

The ADT is just <u>__computations as data__</u>. You cannot use them for program execution, you cannot invoke them.

---

## Step 3: "Lift" your ADT into the Free Monad
<br/>

```scala
// DSL
def printline(out: String): Free[Inout, Unit] = Free.liftF(Printline(out))
def getline: Free[Inout, String] = Free.liftF(Getline)
```

Implement a smart constructor (lowercased function) for each element of your ADT. This is your DSL.

Later we will also use *Free.inject* instead of *Free.liftF*.

---

## Step 4: Write an interpreter for the DSL
<br/>

```scala
// interpreter
object ConsoleInterpreter extends (Inout ~> Id) {

  override def apply[A](fa: Inout[A]): Id[A] = fa match {
    case Printline(out) =>
      println(out)
      () : Id[Unit]
    case Getline =>
      val in = scala.io.StdIn.readLine()
      in : Id[String]
  }
}
```

Every interpreter is/extends a natural transformation from your DSL monad (Inout) to a target monad (Id).
The squiggly arrow is a shortcut for the natural transformation *FunctionK*.
(More on that later in this presentation)

An Interpreter must implement the apply method and is typically a pattern match over the case classes of the ADT.

You can write more than one interpreter for the same ADT.

---

## Step 5: Build a program using the DSL
<br/>

```scala
// program definition (does nothing)
def prog: Free[Inout, (String, Int)] = for {
  _ <- printline("What's your name?")
  name <- getline
  _ <- printline("What's your age?")
  age <- getline
  _ <- printline(s"Hello $name! Your age is $age!")
} yield (name, age.toInt)
````

The program is not executable.

The program can be written with map and flatMap, but is typically a for comprehension written in your DSL.

The programs return type is a *Free[MyADT, RESULT]*.

---

## Step 6: Execute the program
<br/>

```scala
// Execute program with ConsoleInterpreter
val result: Id[(String, Int)] = prog.foldMap(ConsoleInterpreter)

println(s"result = $result")
````

Use *Free.foldMap(...)* to execute the program with a specific intrpreter.

<u>Note:</u> *Free.foldMap* internally uses a technique called Tranpolining.
Trampolining makes the Free Monads stack-safe. No *StackOverflowError*!

---

class: middle, center

# 3. Extending your DSL
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app2.MyApp*
]

---

## Extending your DSL
<br/>

Write a small function as a for comprehension or with map/flatMap.

```scala
def printline(out: String): Free[Inout, Unit] = Free.liftF(Printline(out))
def getline: Free[Inout, String] = Free.liftF(Getline)

def ask(prompt: String): Free[Inout, String] = for {
  _ <- printline(prompt)
  input <- getline
} yield input

def ask2(prompt: String): Free[Inout, String] = // same with flatMap
  printline(prompt).flatMap(_ => getline)
```
<br/>

This allows you to simplify programs written in this DSL.

```scala
def prog: Free[Inout, (String, Int)] = for {
  name <- ask("What's your name?")
  age <- ask("What's your age?")
  _ <- printline(s"Hello $name! Your age is $age!")
} yield (name, age.toInt)
```

---

class: middle, center

# 4. *FunctionK* and
# Natural Transformation
<br/>
<br/>
<br/>
<br/>
<br/>

---

## FunctionK

See also: https://typelevel.org/cats/datatypes/functionk.html
<br/>

*Function1[-A, +B]* takes an *A* and returns a *B*.<br/>
Shortcut: *A => B*
<br/>

```scala
trait Function1[-A, +B] {

  def apply(a: A): B
}
```

*FunctionK[F[\_], G[\_]]* takes an *F[A]* and returns a *G[A]*.<br/>
Shortcut: *F ~> G*
<br/>

```scala
trait FunctionK[F[_], G[_]] {

  def apply[A](fa: F[A]): G[A]
}
```

---

## FunctionK
<br/>

__Function1:__ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; A &nbsp; --> &nbsp; [A => B] &nbsp; --> &nbsp; B<br/>
<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "hello" &nbsp;--> &nbsp; _.length &nbsp; --> &nbsp; 5<br/>
<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; A *Function1* changes a value.<br/>

<br/>

__FunctionK:__ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; F[A] &nbsp; --> &nbsp; [F ~> G] &nbsp; --> &nbsp; G[B]<br/>
<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; List(1, 2) &nbsp;--> &nbsp; _.headOption &nbsp; --> &nbsp; Some(1)<br/>
<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; A *FunctionK* changes the context.

---

## Natural Transformation
<br/>

If the contexts *F* and *G* are Functors, the context conversion is called

<u>__Natural Transformation__</u> or <u>__Functor Transformation__</u>.

---

name: FunctionK_4

## FunctionK

In analogy to *Function1* *FunctionK* also provides methods *compose* and *andThen*.
It also provides a method 'or' which allows to compose two *FunctionK*s, i.e. two interpreters.
(We will use them later.)

```scala
trait FunctionK[F[_], G[_]] { self =>

  // Applies this functor transformation from `F` to `G`
  def apply[A](fa: F[A]): G[A]

  // Composes two instances of FunctionK into a new FunctionK with this
  // transformation applied last.
  def compose[E[_]](f: FunctionK[E, F]): FunctionK[E, G] = ???

  // Composes two instances of FunctionK into a new FunctionK with this
  // transformation applied first.
  def andThen[H[_]](f: FunctionK[G, H]): FunctionK[F, H] = f.compose(self)

  // Composes two instances of FunctionK into a new FunctionK that transforms
  // a [[cats.data.EitherK]] to a single functor.
  // This transformation will be used to transform left `F` values while
  // `h` will be used to transform right `H` values.
  def or[H[_]](h: FunctionK[H, G]): FunctionK[EitherK[F, H, ?], G] = ???
}
```

See: [Usage of *FunctionK.or*](#ComposeDSLs_4)<br/>
See: [Usage of *FunctionK.andThen*](#Routing_1)

---

class: middle, center

# 5. More Interpreters
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app3.MyApp*
]

---

## More Interpreters
<br/>

We can provide several interpreters for the same ADT / DSL.

We can execute a programm written in a DSL with different interpreters for that DSL.

---

## More Interpreters
<br/>

```scala
object ConsoleInterpreter extends (Inout ~> Id) {
  override def apply[A](fa: Inout[A]): Id[A] = ???
}
object AsyncInterpreter extends (Inout ~> Future) {
  override def apply[A](fa: Inout[A]): Future[A] = ???
}
class TestInterpreter(inputs: ListBuffer[String],
                      outputs: ListBuffer[String]) extends (Inout ~> Id) {
  override def apply[A](fa: Inout[A]): Id[A] = ???
}


def prog: Free[Inout, (String, Int)] = for {
  name <- ask("What's your name?")
  age <- ask("What's your age?")
  _ <- printline(s"Hello $name! Your age is $age!")
} yield (name, age.toInt)

val result: Id[(String, Int)] = prog.foldMap(ConsoleInterpreter)

val futureResult: Future[(String, Int)] = prog.foldMap(AsyncInterpreter)

val testResult: Id[(String, Int)] =
                      prog.foldMap(new TestInterpreter(inputs, outputs))
```

---

class: middle, center

# 6. Partial Lifting
# with *Free.inject*
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app3a.MyApp*
]

---

## *Free.inject* instead of *Free.liftF* (1/4)
<br/>

DSL lifted with *Free.liftF*

```scala
def printline(out: String): Free[Inout, Unit] = Free.liftF(Printline(out))
def getline: Free[Inout, String] = Free.liftF(Getline)
def ask(prompt: String): Free[Inout, String] =
  printline(prompt).flatMap(_ => getline)
```

DSL partially lifted with *Free.inject*

```scala
class IoOps[F[_]](implicit IO: InjectK[Inout, F]) {
  def printline(out: String): Free[F, Unit] = Free.inject[Inout, F](Printline(out))
  def getline: Free[F, String] = Free.inject[Inout, F](Getline)
  def ask(prompt: String): Free[F, String] =
    printline(prompt).flatMap(_ => getline)
}

object IoOps {
  // provides an instance of IoOps in implicit scope
  implicit def ioOps[F[_]](
                  implicit IO: InjectK[Inout, F]): IoOps[F] = new IoOps[F]
}
```

*F[_]* is a place holder for another DSL, into which Inout will be injected.

---

## *Free.inject* instead of *Free.liftF* (2/4)
<br/>

- Instead of providing the DSL functions directly, pack them into a class.
- In the class constructor provide an implicit *InjectK[YourDSL, F]*. (*F[_]* is a place holder for some DSL that we provide later.)
- Implement the DSL functions inside the class with *Free.inject*.
- Implement the DSL extension function (*ask*) also inside the new class.
- Provide an implicit instance of this class inside the companion object of the class (= implicit scope).

This is a bit more boilerplate than before.<br/>
But it gives us <u>more flexibility for DSL composition</u>, as we will see later.

---

## *Free.inject* instead of *Free.liftF* (3/4)
<br/>

Program for DSL with *Free.liftF*:

```scala
def prog: Free[Inout, (String, Int)] = for {
  name <- ask("What's your name?")
  age <- ask("What's your age?")
  _ <- printline(s"Hello $name! Your age is $age!")
} yield (name, age.toInt)
```

<br/>
Program for DSL with *Free.inject*:

```scala
def prog(implicit io: IoOps[Inout]): Free[Inout, (String, Int)] = for {
  name <- io.ask("What's your name?")
  age <- io.ask("What's your age?")
  _ <- io.printline(s"Hello $name! Your age is $age!")
} yield (name, age.toInt)
```

---

## *Free.inject* instead of *Free.liftF* (4/4)
<br/>
In the definition of *IoOps* we already defined an *Inout* as the first type parameter of *InjectK[Inout, F]*.
Here in the program definition we replace the place holder DSL *F* with the higher kinded type of another DSL,
which in this case is also Inout. We have composed *Inout* with another *Inout*.

The benefit of this technique becomes obvious shortly.<br/>
We will create one composed DSL out of two different component DSLs.

---

class: middle, center

# 7. Composing two DSLs
# and two Interpreters
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app4.MyApp*
]

---

## Two DSLs

.left-column-50[

### Inout

```scala
trait Inout[A]
final case class Printline(out: String)
        extends Inout[Unit]
final case object Getline
        extends Inout[String]



class IoOps[F[_]](
    implicit IO: InjectK[Inout, F]) {
  def printline(out: String) =
        Free.inject(Printline(out))
  def getline = Free.inject(Getline)
  def ask(prompt: String) =
    printline(prompt)
      .flatMap(_ => getline)
}

object IoOps {
  implicit def ioOps[F[_]](
    implicit IO: InjectK[Inout, F]) =
      new IoOps[F]
}
```
]

.right-column-50[

### KVStore

```scala
trait KVStore[A]
final case class Put(key: String,
    value: Int) extends KVStore[Unit]
final case class Get(key: String
    ) extends KVStore[Option[Int]]
final case class Delete(key: String
    ) extends KVStore[Option[Int]]

class KVSOps[F[_]](
      implicit KV: InjectK[KVStore, F]) {
  def put(key: String, value: Int) =
    Free.inject(Put(key: String, value: Int))
  def get(key: String) =
    Free.inject(Get(key: String))
  def delete(key: String) =
    Free.inject(Delete(key: String))
}

object KVSOps {
  implicit def kvsOps[F[_]](
    implicit IO: InjectK[KVStore, F]) =
      new KVSOps[F]
}
```
]

---

## Two Interpreters

.left-column-50[

### Inout

```scala
object ConsoleInterpreter
            extends (Inout ~> Id) {



  override def apply[A](fa: Inout[A]
          ): Id[A] = fa match {

    case Printline(out) =>
      println(out)
      (): Id[Unit]

    case Getline =>
      val in = scala.io.StdIn.readLine()
      in: Id[String]




  }
}
```
]

.right-column-50[

### KVStore

```scala
object KVSInterpreter 
            extends (dsl.KVStore ~> Id) {

  var kvs: Map[String, Int] = Map.empty

  override def apply[A](fa: KVStore[A]
          ): Id[A] = fa match {

    case Put(key, value) =>
      kvs = kvs.updated(key, value)
      (): Id[Unit]

    case Get(key) =>
      kvs.get(key): Id[Option[Int]]

    case Delete(key) =>
      val value = kvs.get(key)
      kvs = kvs - key
      value: Id[Option[Int]]
  }
}
```
]

<u>Note:</u> Both interpreters have the same target type: *Id*

---

name: ComposeDSLs_4

## Composing DSLs and interpreters

```scala
type AppDSL[A] = EitherK[Inout, KVStore, A]

def prog(implicit io: IoOps[AppDSL],
                  kvs: KVSOps[AppDSL]): Free[AppDSL, (String, Option[Int])] = {
  for {
    name <- io.ask("What's your name?")
    age <- io.ask("What's your age?")
    _ <- kvs.put(name, age.toInt)
    _ <- io.printline(s"Hello $name! Your age is $age!")
    optAge <- kvs.get(name)
  } yield (name, optAge)
}

val composedInterpreter: AppDSL ~> Id =
            (ConsoleInterpreter: Inout ~> Id) or (KVSInterpreter: KVStore ~> Id)
val result: Id[(String, Option[Int])] = prog.foldMap(composedInterpreter)
```

- Define a type alias for an *EitherK* with two ADTs
- Provide DSLs as implicit parameters to your program
- The two component interpreters must have the same target type as the composed interpreter (*Id* in our case).
- The composition order of interpreters must be the same as the composition order of the DSLS.
<br/>
<br/>
See: [Definition of *FunctionK*](#FunctionK_4)

---

## EitherK (= Coproduct)
<br/>

*Either* is parameterized with two types *A* and *B*.<br/>
*A* is the type of the *Left*, *B* the type of the *Right*.

```scala
sealed abstract class Either[+A, +B] ... { ... }
```

*EitherK* is parameterized with two type constructors *F[\_]* and *G[\_]* and a regular type *A*.
It's a case class wrapping a value called *run* of type *Either[F[A], G[A]]*.

```scala
final case class EitherK[F[_], G[_], A](run: Either[F[A], G[A]]) {

  // ...
}
```

*EitherK* is used to define a composed DSL.

In our example we define *AppDSL* as an *EitherK*:

```scala
type AppDSL[A] = EitherK[Inout, KVStore, A]
```

---

## Partial Lifting with *InjectK* (1/2)
<br/>

*InjectK* is used for partial lifting into a Free Monad for the composed DSL.

In class IoOps *Free.inject* internally injects the *Inout* into *InjectK[Inout, F]*,
where the place holder F will be replaced by *AppDSL*.

In class *KVSOps* *Free.inject* internally injects the *KVStore* into *InjectK[KVStore, F]*,
where the place holder *F* will be replaced by *AppDSL*.

```scala
// simplified def of inject
object Free {
  def inject[F[_], G[_], A](fa: F[A])(implicit I: InjectK[F, G]): Free[G, A] =
          liftF(I.inj(fa))
}
```

---

## Partial Lifting with *InjectK* (2/2)
<br/>

```scala
class IoOps[F[_]](implicit IO: InjectK[Inout, F]) {
  def printline(out: String): Free[F, Unit] =
                          Free.inject[Inout, F](Printline(out))
  // ...
}

class KVSOps[F[_]](implicit KV: InjectK[KVStore, F]) {
  def get(key: String): Free[F, Option[Int]] = 
                          Free.inject[KVStore, F](Get(key: String))
  // ...
}

type AppDSL[A] = EitherK[Inout, KVStore, A]

def prog(implicit io: IoOps[AppDSL],
                  kvs: KVSOps[AppDSL]): Free[AppDSL, (String, Option[Int])] = ???
```

In the implicit parameters we fill the place holder *F* with the concrete type *AppDSL* ( = *EitherK[Inout, KVStore, ?]* ).

.note[
For more details see: *app4a.MyApp*
]

---

class: middle, center

# 8. Composing three DSLs
# and three Interpreters
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app5.MyApp*
]

---

## Third DSL and Interpreter
<br/>

Our 3rd DSL and corresponding interpreter(s) is for monotonic sequence number generation.
<br/>
<br/>

```scala
sealed trait Sequence[A]
case object NextId extends Sequence[Long]

class SeqOps[F[_]](implicit KV: InjectK[Sequence, F]) {
  def nextId: Free[F, Long] = Free.inject[Sequence, F](NextId)
  def nextStringId: Free[F, String] = nextId.map(_.toString)
}

object SeqOps {
  implicit def seqOps[F[_]](implicit IO: InjectK[Sequence, F]): SeqOps[F] =
        new SeqOps[F]
}
```

---

## Composing three DSLs and three Interpreters

- Compose 3 DSLs with 2 type aliases.
- Compose interpreters in the same order as the DSLs.
- All component interpreters must have the same target type as the composed interpreter (*Id* in our case).

```scala
type AppDSL0[A] = EitherK[Inout, KVStore, A]
type AppDSL[A] = EitherK[Sequence, AppDSL0, A]

def prog(implicit io: IoOps[AppDSL],
         kvs: KVSOps[AppDSL],
         seq: SeqOps[AppDSL]): Free[AppDSL, (String, Option[Cat])] = {
  for {
    name <- io.ask("What's your name?")
    age <- io.ask("What's your age?")
    id <- seq.nextId.map(_.toString)
    _ <- kvs.put(id, Cat(id, name, age.toInt))
    _ <- io.printline(s"Hello cat $name! Your age is $age!")
    optCat <- kvs.get(id)
  } yield (id, optCat)
}

// compose interpreters in the same order as DSLs
val appInterpreter: AppDSL ~> Id =
        SeqInterpreter or (ConsoleInterpreter or KVSInterpreter)
val result: Id[(String, Option[Cat])] = prog.foldMap(appInterpreter)

println(s"result = $result")
```

---

class: middle, center

# 9. Routing the workflow
# through DSLs
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app6.MyApp*
]

---

## Logging DSL
<br/>

In the next evolution step of the program we create a new DSL for logging and the corresponding interpreters(s).
(interpreter code not shown here)
<br/>
<br/>

```scala
sealed trait Log[A] extends Product with Serializable
final case class Info(msg: String) extends Log[Unit]
final case class Warn(msg: String) extends Log[Unit]
final case class Error(msg: String) extends Log[Unit]

class LogOps[F[_]](implicit LG: InjectK[Log, F]) {
  def info(msg: String): Free[F, Unit] = Free.inject[Log, F](Info(msg))
  def warn(msg: String): Free[F, Unit] = Free.inject[Log, F](Warn(msg))
  def error(msg: String): Free[F, Unit] = Free.inject[Log, F](Error(msg))
}

object LogOps {
  implicit def logOps[F[_]](implicit LG: InjectK[Log, F]): LogOps[F] =
        new LogOps[F]
}
```

---

## Routing the workflow through DSLs (1/2)
<br/>

- We create a composed DSL from *KVStore*, *Sequence* and *Logging*.
- We create new DSL *CatManagement* (cats management business logic).
- The *CatManagement* interpreter is implemented with the above composed interpreter. It routes requests to the other interpreters.
- The main program is implemented in a DSL composed of *Inout* and *CatManagement*.
<br/>
<br/>

---

## Routing the workflow through DSLs (2/2)
<br/>

&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ----------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;| AppDSL |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ----------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;---------------------------- &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Composition<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;------------ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; -----------------------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Inout | &nbsp; &nbsp; &nbsp; &nbsp; | CatManagement |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;------------ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; -----------------------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Routing<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;--------------------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | ComposedDSL |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;--------------------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;------------------------------------------------ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Composition<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;---------------- &nbsp; &nbsp; &nbsp; &nbsp; ---------------- &nbsp; &nbsp; &nbsp; &nbsp; ------------------<br/>
&nbsp; &nbsp; &nbsp; &nbsp; | Logging | &nbsp; &nbsp; &nbsp; | KVStore | &nbsp; &nbsp; &nbsp; | Sequence |<br/>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;---------------- &nbsp; &nbsp; &nbsp; &nbsp; ---------------- &nbsp; &nbsp; &nbsp; &nbsp; ------------------<br/>
<br/>

---

## Composing a DSL from *KVStore*, *Sequence* and *Logging*
<br/>
We compose DSLs and interpreters as we did before.<br/>
<br/>
Component and composed interpreters again with same target type: *Id*
<br/>
<br/>

```scala
// component interpreters, all having target type Id
object LogInterpreter extends (Log ~> Id) { ... }
object SeqInterpreter extends (Sequence ~> Id) { ... }
object KvsInterpreter extends (KVStore ~> Id) { ... }

// compose DSLs
type ComposedDSL0[A] = EitherK[Sequence, KVStore, A]
type ComposedDSL[A] = EitherK[Log, ComposedDSL0, A]

// composed interpreter also with target type Id
val ComposedLogSeqKvsInterpreter: ComposedDSL ~> Id =
          LogInterpreter or (SeqInterpreter or KvsInterpreter)
```

---

## Lifting the composed DSL into a Free Monad
<br/>
We need a monad for the natural transformation from the composed DSL to *Id*.
By creating a type alias we lift *ComposedDSL* into the Free Monad.
Then we define a new interpreter that translates from *FreeComposed ~> Id*
by *foldMap*ping the interpreter we composed before.
<br/>
<br/>

```scala
// type alias for the Free Monad of the composed DSL
type FreeComposed[A] = Free[ComposedDSL, A]

// interpreter that translated from the composed Free Monad to the Id Monad
object FreeComposedLogSeqKvsInterpreter extends (FreeComposed ~> Id) {
  override def apply[A](fa: FreeComposed[A]): Id[A] =
              fa.foldMap(ComposedLogSeqKvsInterpreter)
}
```

---

## CatManagement DSL
<br/>

```scala
sealed trait CatManagement[A] extends Product with Serializable
final case class Create(cat: Cat) extends CatManagement[Cat]
final case class UpdateById(cat: Cat) extends CatManagement[Cat]
final case class DeleteById(id: String) extends CatManagement[Boolean]
final case class FindById(id: String) extends CatManagement[Option[Cat]]
final case class FindByName(name: String) extends CatManagement[List[Cat]]
case object FindAll extends CatManagement[List[Cat]]

class CatOps[F[_]](implicit KV: InjectK[CatManagement, F]) {
  def create(cat: Cat): Free[F, Cat] = Free.inject[CatManagement, F](Create(cat))
  def updateById(cat: Cat): Free[F, Cat] = Free.inject[CatManagement, F](UpdateById(cat))
  def deleteById(id: String): Free[F, Boolean] = Free.inject[CatManagement, F](DeleteById(id))
  def findById(id: String): Free[F, Option[Cat]] = Free.inject[CatManagement, F](FindById(id))
  def findByName(name: String): Free[F, List[Cat]] = Free.inject[CatManagement, F](FindByName(name))
  def findAll: Free[F, List[Cat]] = Free.inject[CatManagement, F](FindAll)
}

object CatOps {
  implicit def catOps[F[_]](implicit CM: InjectK[CatManagement, F]): CatOps[F] =
          new CatOps[F]
}
```

---

## CatLogicInterpreter (1/2)
<br/>
*CatLogicInterpreter* transforms from *CatManagement ~> FreeComposed*
and is implemented with the DSL composed from *Logging*, *Sequence* and *KVStore*.
<br/>

```scala
class CatLogicInterpreter(implicit log: LogOps[ComposedDSL],
                          seq: SeqOps[ComposedDSL],
                          kvs: KVSOps[ComposedDSL])
                          extends (CatManagement ~> FreeComposed) {

  override def apply[A](fa: CatManagement[A]): FreeComposed[A] = fa match {

    case Create(cat) =>
      kvsCreate(cat): FreeComposed[Cat]
    case UpdateById(cat) =>
      kvsUpdateById(cat): FreeComposed[Cat]
    case DeleteById(id) =>
      kvsDeleteById(id): FreeComposed[Boolean]
    case FindById(id) =>
      kvsFindById(id): FreeComposed[Option[Cat]]
    case FindByName(name) =>
      kvsFindByName(name): FreeComposed[List[Cat]]
    case FindAll =>
      kvsFindAll: FreeComposed[List[Cat]]
  }

  // ...
```

---

## CatLogicInterpreter (2/2)
<br/>

```scala
  // ...

  private def kvsFindAll[A]: FreeComposed[List[Cat]] =
    kvs.getAll

  private def kvsFindById[A](id: String): FreeComposed[Option[Cat]] =
    kvs.get(id)

  private def kvsFindByName[A](name: String): FreeComposed[List[Cat]] =
    kvs.getAll.map(_.filter(_.name == name))

  private def kvsCreate[A](cat: Cat): FreeComposed[Cat] =
    for {
      maybeCat <- kvs.get(cat.id)
      _ = if (maybeCat.isDefined) {
        val message = s"cat with id ${cat.id} already exists"
        log.error(message)
        throw new RuntimeException(message)
      }
      newId <- seq.nextStringId
      _ <- kvs.put(newId, cat.copy(id = newId))
      newMaybeCat <- kvs.get(newId)
      _ <- log.info(s"Created: $cat")
    } yield newMaybeCat.get

  // ...
}
```

---

name: Routing_1

## Routing from one interpreter to the next
<br/>
*CatLogicInterpreter* provides a natural transformation (*CatManagement ~> Id*).
It transforms (*CatManagement ~> FreeComposed*) <u>*__andThen__*</u> propagates to *FreeComposedInterpreter*
which transforms (*FreeComposed ~> Id*).
<br/>
<br/>

```scala
// Routing with FunctionK.andThen
val CatLogicInterpreter: CatManagement ~> Id =
              new CatLogicInterpreter andThen FreeComposedLogSeqKvsInterpreter
```

<br/>
See: [Definition of *FunctionK*](#FunctionK_4)

---

## Program definition and execution
<br/>
Technically nothing new here (just DSL composition).
<br/>

```scala
type AppDSL[A] = EitherK[CatManagement, Inout, A]

def prog(implicit io: Inouts[AppDSL],
          co: CatOps[AppDSL]): Free[AppDSL, Option[Cat]] = {
  for {
    name <- io.ask("Cat's name?")
    age <- io.ask("Cat's age?")
    cat <- co.create(Cat(name, age.toInt))
    newAge <- io.ask("That was a lie! Tell me the correct age!")
    _ <- co.updateById(cat.copy(age = newAge.toInt))
    _ <- io.printline(s"Hello cat ${cat.name}! Your age is ${cat.age}!")
    optCat <- co.findById(cat.id)
  } yield optCat
}

val result: Id[Option[Cat]] =
        prog1.foldMap(CatLogicInterpreter or ConsoleInterpreter)
println(s"result = $result")
```

---

class: middle, center

# 10. Pros &amp; Cons
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

.footnote[
.left[
See the following video presentations:<br/>
<br/>
Rather on the Pro side:<br/>
[Chris Myers' Talk on Free Monads at Typelevel Summit Oslo, 2016](#Resources_2)<br/>
Rather on the Cons side:<br/>
[Kelly Robinson's Talk on Free Monads at Scala Days Berlin, 2016](#Resources_2)
]
]

---

## Pros
<br/>
--

- FMs let us create a Monad from any (parameterized) ADT.
--

- FMs let us write programs in monadic style (for comprehensions)
--

- Decoupled program description and execution/interpretation
--

- It is easy to add new ADTs/DSLs.
--

- It is easy to add new interpreters for existing ADTs/DSLs.
--

- E.g.: Implement different interpreters for prod and test.
--

- FMs are stack-safe (due to an internally used technique: <u>Trampolining</u>).

---

## Cons
<br/>
--

- Advanced technology not easily understood.
--

- Danger of over-enginieering
--

- Possible performance cost
<br/>
<br/>
<br/>
--
To minimize drawbacks:<br/>
<br/>
--

- Be aware of the skill level of your team maintaining the code.
--

- Consider using FMs only in library code or in well encapsulated modules.

---

## Principle of Least Power
<br/>

__Given a choice of solutions,__<br/>
__pick the least powerful solution__<br/>
__capable of solving your problem.__

\-- Li Haoyi

---

class: middle, center

# 11. Easing Free Monads with *FreeK*
<br/>
<br/>
<br/>

.footnote[
&nbsp; &nbsp; &nbsp; &nbsp; See: *app5freek.MyApp*
]

---

## *FreeK* - What is it?
<br/>

It is a library, implemented with Shapeless,<br/>
designed to remove some of the boilerplate from your Free Monad code.
<br/>
<br/>

- Write only ADTs, no lifting, no injecting
- Simplified composition of DSLs
- Simplified composition of interpreters

---

## *FreeK* Example: *app5freek.MyApp* (1/2)
<br/>
<br/>

```scala
// Algebra as an ADT
trait Inout[A]
final case class Printline(out: String) extends Inout[Unit]
final case object Getline extends Inout[String]

// DSL to be omitted. No lifting or injecting needed with Freek

// ask is a small subroutine written with FreeK
type PRG = Inout :|: NilDSL
val prg = DSL.Make[PRG]

def ask(prompt: String): Free[prg.Cop, String] = for {
  _ <- Printline(prompt).freek[PRG]
  input <- Getline.freek[PRG]
} yield input

def ask2(prompt: String): Free[prg.Cop, String] =
  Printline(prompt).freek[PRG].flatMap(_ => Getline.freek[PRG])
```

---

## *FreeK* Example: *app5freek.MyApp* (2/2)
<br/>

```scala
type AppDSL = Inout :|: KVStore :|: Sequence :|: NilDSL
val appDSL = DSL.Make[AppDSL] // infer the right coproduct for AppDSL

def prog: Free[appDSL.Cop, (String, Option[Cat])] = {
  for {
    name <- ask("What's your name?").expand[AppDSL] // ask must be expanded
    age <- ask("What's your age?").expand[AppDSL]   // to Printline/Getline
    idLong <- NextId.freek[AppDSL]          // freek performs the heavy lifting
    id = idLong.toString
    _ <- Put(id, Cat(id, name, age.toInt)).freek[AppDSL]
    _ <- Printline(s"Hello cat $name! Your age is $age!").freek[AppDSL]
    optCat <- Get(id).freek[AppDSL]
  } yield (id, optCat)
}

// program execution with foldMap or with interpret
val composedInterpreter =  ConsoleInterpreter :&: KVSInterpreter :&: SeqInterpreter
// foldMap is order-sensitive
val result1: Id[(String, Option[Cat])] = prog.foldMap(composedInterpreter.nat)
println(s"result1 = $result1")
// interpret is order-agnostic
val result2: Id[(String, Option[Cat])] = prog.interpret(composedInterpreter)
println(s"result2 = $result2")
```

---

## *FreeK* - Howto in a nutshell
<br/>
--

- Write your ADTs as before
--

- Remove all the DSL generation code using *Free.liftF* and *Free.inject*.
--

- Write your interpreters as before.
--

- Type alias composed *AppDSL* with the *:|:* operator with *NilDSL* at the end.
--

- The program doesn't need implicit parameters.
--

- "Invoke" the uppercase case class constructors (*Getline* instead of *getline*).
--

- Append an invocation of *freek[AppDSL]* (for the "heavy lifting").
--

- Append *expand[AppDSL]* to your own subroutines (such as *ask*).
--

- Compose interpreters with the *:&amp;:* operator.
--

- Program execution: use *prog.foldMap(myInterpreter.nat)*. *foldMap* is order-sensitive.
--

- Or: Use *prog.interpret(myInterpreter)* and omit the appendix *.nat*. *interpret* is order-agnostic.
<br/>
<br/>
--


Detailed info about *FreeK* at: https://github.com/ProjectSeptemberInc/freek

---

class: middle, center

# 12. Resources

---

name: Resources_1

## Resources (1/3) - basic

<br/>

- Code and Slides of this Talk:<br/>
  https://github.com/hermannhueck/free-monad-app

- Cats documentation on Free Monads:<br/>
  https://typelevel.org/cats/datatypes/freemonad.html

- Blog post on Free Monads by Pere Villega:<br/>
  http://perevillega.com/understanding-free-monads

- Blog post on FreeK by Pere Villega:<br/>
  http://perevillega.com/freek-and-free-monads
  
- The FreeK project on Github<br/>
  https://github.com/ProjectSeptemberInc/freek
  
  

---

name: Resources_2

## Resources (2/3) - basic

<br/>

- "A Year living Freely"<br/>
  Chris Myers' Talk on Free Monads at Typelevel Summit Oslo, 2016<br/>
  https://www.youtube.com/watch?v=rK53C-xyPWw
  
- "Why the free Monad isn't free"<br/>
  Kelly Robinson's Talk on Free Monads at Scala Days Berlin, 2016<br/>
  https://www.youtube.com/watch?v=U0lK0hnbc4U
  
- "Composable application architecture with reasonably priced monads"<br/>
  Runar Bjarnason's Talk on Free Monads at Scala Days Berlin, 2014<br/>
  https://www.youtube.com/watch?v=M258zVn4m2M

---

name: Resources_3

## Resources (3/3) - advanced

<br/>

- Cats documentation on FunctionK:<br/>
  https://typelevel.org/cats/datatypes/functionk.html

- Cats documentation on Free Applicatives:<br/>
  https://typelevel.org/cats/datatypes/freeapplicative.html
  
- "Free as in Monads" - Daniel Spiewak implements Free Monads<br/>
  Daniel Spiewak's live coding session at Northeast Scala Symposium, 2017<br/>
  https://www.youtube.com/watch?v=H28QqxO7Ihc
  
- "Move Over Free Monads: Make Way for Free Applicatives!"<br/>
  John de Goes' Talk on Free Applicatives at Scala World, 2015<br/>
  https://www.youtube.com/watch?v=H28QqxO7Ihc

---

class: middle, center

## Thanks for Listening
<br/>
<br/>
<br/>

# Q &amp; A
<br/>
<br/>
<br/>
<br/>
<br/>

https://github.com/hermannhueck/free-monad-app
