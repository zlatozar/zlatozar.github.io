---
title: "An FMI Exam Task in Scheme, Part 1"
subtitle: "Counting atoms in a list, one commandment from The Little Schemer at a time."
description: "A functional programming exam task from FMI solved in Scheme: count how often each atom appears in a list, with recursion, cons, accumulators, and The Little Schemer commandments."
date: 2012-08-13 18:42:00 +0300
categories: [Programming]
tags: [lisp, scheme, racket, functional-programming, recursion, the-little-schemer]
---

Two months ago someone showed me a task from a functional programming exam at FMI, the
Faculty of Mathematics and Informatics. Students had 2 hours to solve it. Here is roughly
how it looked:

> **The task.** Write a Scheme program that computes how often each atom appears in a
> given list. Example:
>
> ```text
> Input:  '(a a b c)
> Output: '((2 a) (1 b) (1 c))
> ```

I had not written Scheme for at least four years, so I was not sure I could do it in time.
But it is a challenge, and I decided to refresh my memory: can I solve it within the same
2 hours?

I have read two books on the topic: [How to Design Programs](https://htdp.org/) and
[The Little Schemer](https://mitpress.mit.edu/9780262560993/the-little-schemer/), formerly
known as *The Little LISPer*. The commandments and laws in the boxes below come from
*The Little Schemer*.

I use Emacs for everything except Java, so the natural choice was to set it up for Scheme.
There is not much choice, so I quickly set up [Geiser](https://www.nongnu.org/geiser/).
Things did not go well. I had forgotten a lot and had to debug, and Geiser did not help
me there. So [DrRacket](https://racket-lang.org/) came to the rescue. It works very well.
The only problem: I could not make its key shortcuts work like Emacs, so I lost paredit.

To be honest, I did not finish the task within two hours. Here are my steps, mixed with my
reasoning.

## Warming Up

To warm up and to test my environment, I gave myself a smaller task:

> **Warm-up.** Check whether a given atom belongs to a given list. Example:
>
> ```text
> Input:  'a '(a b c)
> Output: #t
> ```

It was not hard:

```scheme
(define in-trace?
  (lambda (a lat)
    (cond
      ((null? lat) #f)
      ((eq? a (car lat)) #t)
      (else
       (in-trace? a (cdr lat))))))

;; (in-trace? 'a '(a b c)) ;; #t
```

> **Note**
>
> `(define (in-trace? a lat) ...)` is syntactic sugar for
> `(define in-trace? (lambda (a lat) ...))`.
{: .callout .callout--note}

> **The Law of Car**
>
> The primitive `car` is defined only for non-empty lists.
{: .callout .callout--commandment}

> **The Law of Cdr**
>
> The primitive `cdr` is defined only for non-empty lists. The `cdr` of any non-empty list
> is always another list.
{: .callout .callout--commandment}

> **Scheme style.** Affix a question mark to the end of a name for a procedure whose
> purpose is to ask a question of an object and to yield a boolean answer.
>
> Pronounce the question mark as if it were the isolated letter "p". For example, to read
> the fragment `(PAIR? OBJECT)` aloud, say: "pair-pee object."
>
> — Taylor R. Campbell,
> [Riastradh's Lisp Style Rules](https://mumble.net/~campbell/scheme/style.txt)

## The Algorithm First

The most important part, and the one I underestimated, is the algorithm itself. I did not
play it through with pen and paper first. I wrote code in a chaotic way. I was worried
about my Scheme knowledge, I wanted everything to be very cool, and I wanted to prove
myself. This slowed me down.

> **The Sixth Commandment**
>
> Simplify [make it cool] only after the function is correct.
{: .callout .callout--commandment}

This is the algorithm I chose:

> Take the first atom and count how many times it appears. Write the result down. Then
> reduce the list by removing this atom, and repeat. Example:
>
> ```text
> (a a b c c) -> (2 a)
> (b c c)     -> (1 b)
> (c c)       -> (2 c)
> ```

## Counting One Atom

This part was quick, and I learned some lessons:

```scheme
(define count-trace
  (lambda (a lat)
    (cond
      ((null? lat) 0)
      ((eq? a (car lat)) (+ 1 (count-trace a (cdr lat))))
      (else
       (count-trace a (cdr lat))))))

;; (count-trace 'a '(a b a a a a c)) ;; 5
```

> **The Fourth Commandment (preliminary version)**
>
> Always change at least one argument while recurring. It must be changed to be closer to
> termination. The changing argument must be tested in the termination condition: when
> using `cdr`, test the termination with `null?`.
{: .callout .callout--commandment}

I imagine recursion as a spiral. First it winds up, until the check `((null? lat) 0)`
stops it. Then it starts to unwind, and each call returns the value of its last line, the
expression around the recursive call. This is where the solution is built.

> **The First Commandment (final version)**
>
> When recurring on a list of atoms, `lat`, ask two questions about it: `(null? lat)` and
> `else`.
>
> When recurring on a number, `n`, ask two questions about it: `(zero? n)` and `else`.
>
> When recurring on a list of S-expressions, `l`, ask three questions about it:
> `(null? l)`, `(atom? (car l))`, and `else`.
{: .callout .callout--commandment}

## Removing an Atom

The next step is to build the reduced list:

```scheme
(define reduce
  (lambda (a lat)
    (cond
      ((null? lat) '())
      (else (cond
              ((eq? (car lat) a) (reduce a (cdr lat)))
              (else (cons (car lat) (reduce a (cdr lat)))))))))

;; (reduce 'a '(a b c a)) ;; '(b c)
```

Have you asked yourself what happens when no question in a `cond` is true? The result is
not specified. That is why every `cond` should end with `else`, and `else` should not
hide more questions inside it.

As in Common Lisp, `(cond ... (else ...))` can also be written as `(cond ... (#t ...))`.

I refactor at once. All questions go above `else`, and `else` keeps only what is left:

```scheme
(define reduce
  (lambda (a lat)
    (cond
      ((null? lat) '())
      ((eq? (car lat) a) (reduce a (cdr lat)))
      (else
       (cons (car lat) (reduce a (cdr lat)))))))

;; (reduce 'a '(a b c a)) ;; '(b c)
```

## Three Rules About cons

Here are three very important rules:

> **The Second Commandment**
>
> Use `cons` to build lists.
{: .callout .callout--commandment}

When I first read a book about Lisp, I thought `cons` was very easy. It turned out I was
not quite right. If you think so too, then why does this happen?

```scheme
> (cons '(1 2) '(3 4))
((1 2) 3 4)
```

**Hint:** How is this represented in memory?

> **The Golden Rule of cons**
>
> The second argument to `cons` should be a list, and every list ends with `'()`.
{: .callout .callout--key}

Here are some explanations that I find useful:

> `cons` builds pairs, not lists! Lisp interpreters use a dot to separate the elements of
> a pair visually. `car` and `cdr` return the first and the second element of a pair.
>
> Lists are built on top of pairs. If the `cdr` of a pair points to another pair, the
> sequence is treated as a list.
>
> The `cdr` of the last pair points to a special object called null (written as `'()`).
> It tells the interpreter that it has reached the end of the list.
>
> For example, the list `'(a b c)` is built by evaluating this expression:
>
> ```scheme
> (cons 'a (cons 'b (cons 'c '())))
> ;; (a b c)
> ```
>
> The `list` procedure is a shortcut for creating lists:
>
> ```scheme
> (list 'a 'b 'c)
> ;; (a b c)
> ```
>
> Another way to think of sequences whose elements are sequences is as trees. The elements
> of the sequence are the branches of the tree, and elements that are themselves sequences
> are subtrees.
>
> — The last paragraph is from
> [SICP, section 2.2.2](https://mitp-content-server.mit.edu/books/content/sectbyfn/books_pres_0/6515/sicp.zip/full-text/book/book-Z-H-15.html)

> **The Third Commandment**
>
> When building lists, describe the first typical element, and then `cons` it onto the
> natural recursion.
{: .callout .callout--commandment}

Here is what the last commandment means in `reduce`:

```text
(else (cons (car lat) (reduce a (cdr lat))))
            |_______| |__________________|
             typical   natural recursion
                   (recur on the rest of lat)
```

## A First Draft

Hey, are we done?

```scheme
(define freq
  (lambda (lst)
    (define freq-in
      (lambda (lat seen)
        (cond
          ((null? lat) seen)
          (else
           (freq-in (reduce (car lat) lat)
                    (cons (cons (count-trace (car lat) lat) (car lat)) seen))))))
    (freq-in lst '())))

;; (freq '(a a a c b e f c f g t t w w w w w g h))
;; '((1 . h) (5 . w) (2 . t) (2 . g) (2 . f) (1 . e) (1 . b) (2 . c) (3 . a))
```

I use an inner helper function, `freq-in`, so the caller does not see the extra `seen`
parameter. Not bad, right? But look at the output. There are two problems:

1. The atoms come out in reverse order. This is a very common problem in Lisp when you
   collect results in an accumulator.
2. The task asks for `(3 a)`, but I get `(3 . a)`: a pair, not a list. I broke the golden
   rule of `cons`. The second argument was an atom, not a list.

The second problem is easy to fix: `(list count atom)` instead of `(cons count atom)`. For
the first one I need a function that reverses a list:

```scheme
(define revert
  (lambda (lst)
    (define revert-in
      (lambda (lat rev)
        (cond
          ((null? lat) rev)
          (else
           (revert-in (cdr lat) (cons (car lat) rev))))))
    (revert-in lst '())))

;; (revert '(a b c)) ;; '(c b a)
;; (revert '((1 1) (2 2) (3 3))) ;; '((3 3) (2 2) (1 1))
```

I did not write it quickly. This is how I reasoned:

> I will use recursion, so I have to reduce the problem of reversing `(1 2 3 4)` with
> `car` and `cdr`. Step by step, the two arguments of `revert-in` are:
>
> ```text
> (1 2 3 4)  ()
> (2 3 4)    (1)
> (3 4)      (2 1)
> (4)        (3 2 1)
> ()         (4 3 2 1)
> ```
>
> I noticed that I always pass the `cdr` as the first argument, and let the second one
> build the result with `cons`.

## Tracing the Recursion by Hand

You may prefer another method: write out the recursion by hand. This example is from
*The Little Schemer*:

```scheme
;; The rember function removes the first occurrence
;; of the given atom from the given list.
(define rember
  (lambda (a lat)
    (cond
      ((null? lat) '())
      ((eq? (car lat) a) (cdr lat))
      (else (cons (car lat)
                  (rember a (cdr lat)))))))

;; (rember 'cup '(coffee mock cup tea cup hick)) ;; '(coffee mock tea cup hick)
```

The hardest line is `((eq? (car lat) a) (cdr lat))`. How is the result list built?

```scheme
(rember 'cup '(coffee mock cup tea cup hick))
= (cons 'coffee (rember 'cup '(mock cup tea cup hick)))
= (cons 'coffee (cons 'mock (rember 'cup '(cup tea cup hick))))
;; here (eq? (car lat) a) is true, so rember returns (cdr lat)
= (cons 'coffee (cons 'mock '(tea cup hick)))
= '(coffee mock tea cup hick)
```

## The Final Solution

Feeling confident, I write the final solution, using this rule:

> **The Eighth Commandment**
>
> Use help functions to abstract from representations.
{: .callout .callout--commandment}

```scheme
(define freq-final
  (lambda (lst)
    (define freq-in
      (lambda (lat seen)
        (cond
          ((null? lat) (revert seen))
          (else
           (freq-in (reduce (car lat) lat)
                    (cons (list (count-trace (car lat) lat) (car lat)) seen))))))
    (freq-in lst '())))

;; (freq-final '(a a a c b e f c f g t t w w w w w g h))
;; '((3 a) (2 c) (1 b) (1 e) (2 f) (2 g) (2 t) (5 w) (1 h))

;; (freq-final '(a a b c)) ;; '((2 a) (1 b) (1 c))

;; (freq-final '()) ;; '()
```

> **Where to reverse**
>
> The first version of this post called `revert` around the recursive call. Then every
> return reversed the list once more, and the order was right only when the number of
> different atoms was odd. Both examples above have an odd number, so the bug stayed
> hidden: `(a b)` gave `((1 b) (1 a))`. Reverse once, when the accumulator is complete.
{: .callout .callout--note}

As a solution for an exam task, this is enough. But can we do something more? I have to
think!

This `count-trace` ties the function to one way of counting. We may want something else,
for example a `count-odd-trace`. So I pass the counting function as a parameter:

```scheme
(define freq-*
  (lambda (lst func)
    (define freq-in
      (lambda (lat seen)
        (cond
          ((null? lat) (revert seen))
          (else
           (freq-in (reduce (car lat) lat)
                    (cons (list (func (car lat) lat) (car lat)) seen))))))
    (freq-in lst '())))

;; (freq-* '(a a b c) count-trace) ;; '((2 a) (1 b) (1 c))
```

> **The Ninth Commandment**
>
> Abstract common patterns with a new function.
{: .callout .callout--commandment}

> **From lambda calculus.** One thing we do know how to do is: *we know how to get rid of
> free variables.*
>
> Think about it: why is `x` free in `(lambda (y) x)`? Because it does not look like this:
> `(lambda (x) (lambda (y) x))`. Well, if we want it to look like that, let's just make it
> be that! After all, we are not changing the value of the expression, we are only
> changing the way the free variable will derive its meaning.
>
> **We are promising to pass in the value rather than rely on the rules of lexical scoping
> to ascribe the right value from the static (lexically apparent) context. This process of
> factoring out free variables is called abstraction.**

**Update:** After reading
[The Seasoned Schemer](https://mitpress.mit.edu/9780262561006/the-seasoned-schemer/), I
saw that a `let` is handy here. A named `let` removes the repeated `(car lat)` and the
separate helper definition that only starts the loop:

```scheme
(define freq-*
  (lambda (lst func)
    (let freq-in ((lat lst) (seen '()))
      (cond
        ((null? lat) (revert seen))
        (else
         (let ((curr (car lat)))
           (freq-in (reduce curr lat)
                    (cons (list (func curr lat) curr) seen))))))))
```

> **The Fifteenth Commandment (revised version)**
>
> Use `(let ...)` to name the values of repeated expressions in a function definition if
> they may be evaluated twice for one and the same use of the function.
{: .callout .callout--commandment}

With this abstraction, I can easily build other functions on top of it:

```scheme
(define count-freq
  (lambda (lat)
    (freq-* lat count-trace)))

;; (count-freq '(a a b c)) ;; '((2 a) (1 b) (1 c))
```

## Do We Get a Six?

In Bulgaria, 6 is the highest grade. Do we get a 6? Yes, if the algorithm I chose is
correct and its complexity is not too high. The rule for judging it is:

> **The asymptotic outlook.** Ask not which takes longer, but rather which is more rapidly
> taking longer as the problem size increases.

I look at the final result and I do not like it. I wrote it by wandering around, even
though it is correct. No Lisp programmer would write it this way. It has no style; it is
just a set of rules put together. [Part 2]({% post_url 2012-10-15-fmi-task-part-2 %})
follows with the fixes.
