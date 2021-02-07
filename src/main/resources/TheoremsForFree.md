Theorems for free!
==================

Disclaimer
----------

This is just transcription of Philip Wadler paper ["Theorems for free"](http://mng.bz/Z9f1)
The aim of this document is to excerpt the basic information form that
document in a developer friendly fashion. I encourage you to read original paper (propably after reading this).


Abstract
--------

From the signature of a polymorphic function we can derive a theorem that it satisfies. 
Every function of the same signature satisfies the same theorem.
This provides a free source of useful theorems, courtesy of 
Reynolds' abstration theorem for the polymorphic lambda calculus.

Section 1: Introduction
-----------------------

Write down the definition of a polymorphic function on a piece of paper. Tell me 
its signature, but be careful not to let me see the function's body. I will tell you
a theorem that the function satisfies.

The purpose of this paper is to explain the trick. But first, let's look at an example.

Say that `r` is a function of signature:

```def r[X](List[X]): List[X]```

Here `X` is a type variable.
From this, as we shall see, it is possible to conclude that `r` satisfies the following
theorem: for all types `A1` and `A2` and every total function `def f_a(x: A1): A2` we have

```r[A1](list).map(f_a) = r[A2](list.map(f_a))```

for any `list` of type `List[X]`.

Actually in Scala, concrete type `X` could be inferred by the compiler, so we could 
write the above in this way:

```r(list).map(f_a) = r(list.map(f_a))```

A note about nomenclature. 

In above "code" `=` is not assignment but equivalence. 
We want to say that both (combined) functions are the same
(i.e. always yields the same results fo the sam arguments).
Next thing is that a function have it's signature and also its type. Which is 
more or less the same. For instance
`def r[X](List[X]): List[X]` has type `r[X]: List[X] => List[X]`
The type would be easier to use later oos it will be used extensively.
Let's rewrite above example using types. We also do not need `list` variable. It's redundant
we could use Scala lambda notation with the underscore `_`

thus:

for all functions `r` and `f_a` of types `r[x]: List[X] => List[X]` 
and `f_a[A1, A2]: A1 => A2` accordingly, it holds that

```r(_).map(f_a) = r(_.map(f_a))```

To be hones the right-hand side isn't valid Scala syntax, but it should be interpreted as 
the following lambda `l => r(l.map(f_a))` which is IMO pretty straightforward.

The intuitive explanation of this result is that `r` must work on lists of `X`
for *any* type `X`. Since `r` is provided with no operations on values of type `X`, 
all it can do, is to rearrange such lists, independent of the values contained in them.
Thus applying `f_a` to each element of a list an then rearranging yields 
the same result as rearranging and then applying `f_a` to each element.

For example, `r` may be the function `def reverse[X](list: List[X]): List[X]` 
(or in alternative notation `reverse[X]: List[X] => List[X]`) that
reverses a list, and `f_a` may be function `def code(c: Char): Int` 
(or alternatively `code: Char => Int`) that converts a character to
its ASCII code. Then we have

```
reverse(List('a', 'b', 'c')).map(code) 
 = List('c', 'b', 'a').map(code)
 = List(99, 98, 97) 
 = reverse(List(97, 98, 99)
 = reverse(List('a', 'b', 'c').map(code))
```

which satisfies the theorem. 

Or `r` may be the function `tail[X]: List[X] => List[X]`
that returns all but the first element of a list, and `f_a` may be the function
`inc: Int => Int` that adds one to an integer. Then we have

```
tail(List(1, 2, 3)).map(inc) 
 = List(3, 4)
 = tail(List(1, 2, 3).map(inc))
```
which also satisfies the theorem.

On the other hand, say `r` is the function 
`odds: List[Int] => List[Int]` that retains only odd elements on the list
of integers, and say `f_a` is `inc` as before. Now we have

```
odds(List(1, 2, 3)).map(f_a) 
 = List(2, 4)
 != List(3)
 = odds(List(1, 2, 3).map(f_a))
```

and that theorem is *not* satisfied. But this is not a counter example, because 
`odds` has wrong type: it is too specific, `List[Int] => List[Int]` 
rather than `[X]: List[X] => List[X]`.

Well, well, but I can find a function that breaks the law anyway. 
Let's say

```
  def r[X](list: List[X]): List[X] = list.filter(_.hashCode() % 2 == 0)
```

And yeah, that's true, but it is also too specific. 
The `r` is restricted only to lists of items that extends `java.lang.Object` interface. Actually,
all generics are, but this is not mathematical property, but a JVM's restriction.
Let's assume that methods from `jaca.lanf.Object` interface are not accessible and
then try to write a function that breaks the law.

This example theorem about functions of type `r[X]: List[X] => List[X]` is pleasant but not earth-shaking. 
What is more exciting is that similar theorem could be derived for *every type*.


The result that allows theorem to be derived from types will be referred 
to as the *parametricity* result, because it depends in an essential way on parametric
polymorphism (types of the form `[X]: T(X)`). Parametricity is just a reforulation of Reynolds' 
abstation theorem: terms evaluated in related enviromnetns yields related values. The key idea 
is that types may be read as relations. The result will be explained in Section 2 and stated
more formally in Section 6. 

Some further applications of parametricy are shown below, which shows several types and corresponding theorems.
Each name was chosen, of course, to suggest a particular function of the named type (signature),
but the associated theorems hold fo *any* function that has the same type (so long as it can be defined
as a term in pure polymorphic lambda calculus). For example the theorem given for `head` also holds for `last`
and the theorem for `sort` also holds for `nub`.


```
Assume:
def f_a[A1, A2](a: A1): A2
def f_b[B1, B2](b: B1): B2

(or alternatively)
f_a: A1 => A2
f_b: B1 => B2
```

then
```
for: 
head[X]: List[X] => X
f_a(head(_)) = head(_.map(f_a)
```
```
and for:
identity[X]: X => X
f_a(identity(_)) = identity(f_a(_))
```
```
and for:
tail[X]: List[X] => List[X]
tail(_).map(f_a) = f_a(tail(_))
```
```
and for:
concat[X]: (List[X], List[X]) => List[X]
concat(_, _).map(f_a) = concat(_.map(f_a), _.map(f_a))
```
```
and for:
flatten[X]: List[List[X]]: List[X]
flatten(_).map(f_a) = flatten(_.map(_.map(f_a)))
```
```
and for:
first[X, Y]: (X, Y) => X
pair => f_a(first(pair)) = pari => first(f_a(pair._1), f_b(pair._2))
```
```
and for:
second[X, Y]: (X, Y) => Y
pair => f_b(second(pair)) = pair => second(f_a(pair._1), f_b(pair._2))
```
```
and for:
key[X, Y]: X => Y => X
x => y => f_a(key(x)(y)) = key(f_a(x))(f_b(y))
```
a little commentary: key signature could look like `def key[X,Y](x: X) => Y => X`  
and this is very similar `first` function but instead of using pairs we use currying. 
What is curring? You might ask. Throughout this paper we assume that every function
accepts exactly one parameter, but in Scala they can accept any number of them. We could
fix that easily. In two ways - accepting a tuple, or currying 
(tranferring a function to a chain of functions). See example:
```
def many_params(a: Int, b: Int, c:Int): Int = a * b - c
val many_params_lambda: (Int, Int, Int) => Int = many_params
val many_params_result: Int = many_params(1, 2, 3)

// many_params could be tranformed to a function with single paramter - a tuple
def accepts_tuple(t: (Int, Int, Int)): Int = t._1 * t._2 - t._3
val accepts_tuple_lambda: ((Int, Int, Int)) => Int = accepts_tupel
val accepts_tuple_result: Int = accepts_tuple((1, 2, 3))

// or using curring
def curried(a: Int): Int => Int =  Int = a => b => c => a * b - c
def curreid_lambda: Int => Int => Int => Int = curried
val partial_application: Int => Int => Int = curreid(1)
val curreid_result: Int = curried(1)(2)(3)
```
```
and for:
zip[X, Y]: (List[X], List[Y]) => List[(X, Y)]
case (xs, ys) => zip(xs, yx).map { case (x, y) => (f_a(x), f_b(y)) } 
 = case (xs, ys) => zip(xs.map(f_a), ys.map(f_b))
```
a little commentary, `zip` signature would look like: `def zip[X, Y](xs: List[X], ys: List[Y]): List[(X,Y)]`
```  
and for:
 filter[X]: (X => Boolean) => List[X] => List[X]
xs: List[A1] => filter(x => p2(f_a(x)) 
 = xs: List[A1] => filter(p2)(xs.map(f_a))
```

A not that little commentary, `filter` signature would look like: 
`def filter[X](predicate: X => Boolean): List[X] => List[X]`.
W have an example of curried function here 
(filter takes predicate, and return a function, that in turn takes a list and returns a filtered list).
Additionally `p2` is any predicate of type `p2: A2 => Boolean`
```
and for:
sort[X]: ((X, X) => Boolean) => List[X] => List[X]
if for all (a1, a2) of type X (a1 cmp1 a2) = (f_a(a1) cmp2 f_a(a2))
then:
xs => sort(cmp1)(xs).map(f_a) = sort(cmp2)(xs.map(f_a))
```
Commentary: 
 * `sort` signature: `def sort[X](compare: (X, X) => Boolean): List[X] => List[X]`
 * let's say that `f_a` maps type `X` to `Y` then
   * `cmp1` is of type `cmp1: (X, X) => Boolean` and 
   * `cmp2` is of type `cmp2: (Y, Y) => Boolean`
 * type `X` is ordered by `cmp1` and type `Y` by `cmp2`, the function `f_a` keeps the ordering - i.e. 
   mapped elements are in the same order than arguments. (`f_a` is strictly increasing)
```
and for:
fold[X, Y]: (X => Y => Y) => Y => List[X] => Y
if for all (x, y) f_b(comb1(x, y)) = comb2(f_a(x)(f_b(y))
then
xs => f_b(fold(comb1)(zero)(xs)) = xs => fold(comb2)(f_b(zero))(xs.map(f_a))
```
Commentary:
 * `def fold[X, Y](combine: X => Y => Y) => Y => List[X] => Y`
 * note that this is equivalent to more "Scala typical" signature 
   `def fold[X, Y](list: List[X], zero: Y)(combine: (X, Y) => Y)`, 
   but order of arguments is reversed
```
for all (a, b) of types A1 and B1 respectively and
comb1[A1, B1]: (A1, B1) => B1
comb2[A2, B2]: (A2, B2) => B2
zero: B1
```

Still hard? let's take an example:

```
type A1 = Int
type A2 = Double

type B1 = List[Int]
type B2 = List[Double]

// f_a: A1 => A2
def f_a(x: Int): Double = Math.sqrt(x.toDouble)

// f_b: B1 => B2
def f_b(l: List[Int]): List[Double] = l.map(i => Math.sqrt(i.toDouble))

// comb1: (A1, B1) => B1
def comb1(a: Int, b: List[Int]): List[Int] = a :: b

// comb2: (A2, B2) => B2
def comb2(a: Double, b: List[Double]): List[Double] = a :: b

zero = List.empty[Int]
f_b(zero) = List.empty[Double]

let xs: List[Int] = List(16, 9)

f_b(fold(comb1)(zero)(xs)) 
 = f_b(fold(comb1)(Nil[Int])(xs)) 
 = f_b(fold(comb1)(Nil[Int])(List(16, 9))) 
 = f_b(List(16, 9))
 = List(4.0, 3.0)

fold(comb2)(f_b(zero))(xs.map(f_a)) 
 = fold(comb2)(Nil[Doule])(List(16, 9).map(f_a))
 = fold(comb2)(Nil[Double])(List(4.0, 3.0) 
 = List(4.0, 3.0) 
```

Section 2 parametricity explained
---------------------------------

The key to extract theorems from types is to read types as relations (in mathematical sense).
This section outlines the essential ideas, using a naive model of the polymorphic
lambda calculus: types are sets, functions are set-theoretic functions etc.
This sesction sticks to the simple but naive view, it will be refined in 
Sections 4-6 which explain the same notation in context of frame models.

The usual way to read a type is as a **set**.
 * for primitive types, the type is associated with set of its possible values. 
   * The type `Boolean` corresponds to the set of booleans (i.e. `{ true, false }`)
   * the type `Int` corresponds to the set of integers 
     `{0, -1, 1, 2, -2, 3, ..., Int.MinVale, Int.MaxValue}`
 * If `A` and `B` are types then `(A, B)` type correspond to set of pairs drawn from `A` and `B`. 
   * For instance type `(Boolean, Int)` could be interpreted as set 
     `{ (false, 0), (true, 0), (false, -1), ... }`
 * The type `List[A]` corresponds to the set of lists of A
   *  `List[Boolean]` corresponds to `{ [], [true], [true, true], [false], ... }`
 * The type `A => B` corresponds to set of functions of that type
    * `String => Boolean` corresponds to `{ _.isEmpty, _.length > 3, ... }`
 * Further if `X` is type variable (a generic parameter) and `T(X)` is a type
   dependent on `X` then type `[X]: T(X)` corresponds to a set of functions
   that takes set (type) `Q` and returns an element in `T(Q)`
   * This is harder to comprehend, as this is not someting you typically do when programing. Let's go through an example.
   * Let `T(X) = X => List[X]` - so we assign type `X => List[X]` to any type `X`
   * Such projection could be noted as `def T[X]: X => List[X] = ???`
   * so the type `[X]: X => List[X]` is the set of functions that for type `X` assigns type `X => List[X]`
   * so it contains e.g. `{ def t1[X]: X => List[X] = x => List(x); def t2[X]: X => List[X] = x => List(x, x) ...}` 

An alternative representation is to read a type as a relation. What are relations? 
Quick remainder from algebra course. Due to markdown restrictions I would use letter `c` 
instead of &#x220A; to describe that an element belongs to a set. And `C` 
instead of &#x2282; to describe that a set is subset of another.


If `A1` and `A2` are sets we write `_A_: A1 <=> A2` to indicate that `_A_` is relation between `A1` and `A2`,
that is that `_A_ C A1 x A2` (`A1 x A2` is cartesian product of sets `A1`, `A2`).

If `x1 c A1` and `x2 c A2`, we write `(x1, x2) c _A_` to 
indicate that `x1` and `x2` are related by `_A_`. A special case of relation is identity relation
`_I_: A <=> A` defined by `_I_ = { (x, x) | x c A}`. In other words, if `x1`, `x2` are
elements of set `A` then `(x1, x2) c _I_` iff `x1 = x2`.

More generally, any function `f_a: A1 => A2` may also be read as relation: `{(x, f_a(x)) | x c A1}`. 
In other words, if `x1 c A1` and `x2 c A2` then `(x1, x2) c f_a` iff `f_a(x1) = x2`.

To read types as relations, we give a relational equivalent for simple types and for each type constructors: 
tuple `(A, B)` list `List[A]`, function `A => B` and higher kinded function `[X]: T(X)`. 
 * Simple types such as `Boolean` and `Int`, may simply be read as identity relations 
   `_I_Boolean: Boolean <=> Boolean` and `_I_Int: Int <=> Int`. So they are e.g.
   * `_I_Boolean = { (true, ture), (false, false) }`
   * `_I_Int = { (0, 0), (-1, -1), (1, 1), (-2, -2), ... }`
 * For any relations `_A_: A1 <=> A2` and `_B_: B1 <=> B2` the relation 
   `(_A_, _B_): (A1, B1) <=> (A2, B2)` is defined by
   ```
      ((x1, x1), (y1, y2))  c  (_A_, _B_)
      iff
      (x1, x2) c _A_ and (y1, y2) c _B_
   ```
   * That is, pairs are related if ther corresponding components are related.
   * Example `(Int, Boolean) = { ((0, 0), (true, true)), ((0, 0), (false, false)), ((-1, -1), (true, true)) }`
 * For any relations `_A_: A1 <=> A2` and `_B_: B1 <=> B2`, the relation 
   `_A_ => _B_: (A1 => B1) <=> (A2 => B2)` is defined by
   ```
   (f1, f2) c _A_ => _B_
   iff
   for all (x1, x2) c _A_, (f1(x1), f2(x2)) c _B_

   ```
   * That is, functions are related if they take related arguments into related results. In teh special case where 
    `f_a` and `f_b` are functions, relations `f_a => f_b` will not necessarily be a function, but in case 
     `(f1, f2) c f_a => f_b` is equivalent `f2(f_a(_)) = f_b(f1(_))`
   * Example `Int => Boolean = { (_ == 0, _ == 0), (_ < 2, _ <2), ... }` Here we have a set of pairs of functions
   

Section 3 parametricity applied
-------------------------------

This section first explains in detail ho parametricyt implies some of the theorems listend in the introduction and then
presets some more general results.

### 3.1 Rearrangements
The result in the introducation is a simple conseqence of parametricyt. Let `r` be a function of type 

```r[X]: List[X] => List[X]```

Parametricity ensures that

```(r, r) c [X]: List[X] => List[X]```

By the definiton of `[X]` relations this is equivalent to 

```
for all _A_: A1 <=> A2,
  (r[A1], r[A2]) c List[A1] => List[A2]
```

By definition of `=>` on relations, this in turn is equivalent to 

```
for all _A_: A1 <=> A2
  for all (xs1, xs1) c List[_A_],
    (r[A1](xs1), r[A2](xs2)  c List[_A_]
```

This could be further expended in terms of definition of `List[_A_]`.
A more convienient version can be derived by specializing to the case when 
the relation `_A_` is a function `f_a: A1 => A2`. The above becomes

```
for all f_a: A1 => A2
  for all xs,
    xs.map(f_a) = xs2 implies r[A1](xs).map(f_a) = r[A2](xs2)
```
or eqivalently,
```
for all f_a: A1 => A2
  r(_).map(f_a) = r(_.map(f_a))
```

This is the version given in introduction.