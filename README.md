# Use Applicative where applicable!

### (Examples implemented with *Cats*)

Most Scala developers are familiar with monadic precessing.
Monads provide *flatMap* and hence for-comprehensions
(syntactic sugar for *map* and *flatMap*).

Often we don't need Monads.
Applicatives are sufficient in many cases.

In this talk I examine the differences between monadic and applicative processing
and give some guide lines when to use which.

After a closer look to the Applicative trait
I will contrast the gist of *Either* and *cats.data.Validated* (the latter being an Applicative but not a Monad).

I will also look at traversing and sequencing
which harness Applicatives as well.
