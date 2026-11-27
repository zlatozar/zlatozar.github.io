---
title: "An FMI Exam Task in Scheme, Part 2"
subtitle: "Better names, a natural recursion, and nothing to reverse."
description: "Part 2 of a Scheme exam task from FMI: better names for reduce and revert, what reverse costs, a natural recursion that needs no accumulator, and letrec from The Seasoned Schemer."
date: 2012-10-15 19:30:00 +0300
categories: [Programming]
tags: [lisp, scheme, racket, functional-programming, recursion, the-seasoned-schemer]
---

In [Part 1]({% post_url 2012-08-13-fmi-task-part-1 %}) I described how I reasoned while
solving a task from an FMI exam. It all went in a rather chaotic way, so the final result
gives correct answers, but it does not satisfy me. By the way, the person who showed me
the task passed the exam, although he had to explain at length why he had not solved
this one :)

Here is the code from Part 1 again. So where is the mistake?

```scheme
;; nothing to change
(define count-trace
  (lambda (a lat)
    (cond
      ((null? lat) 0)
      ((eq? a (car lat)) (+ 1 (count-trace a (cdr lat))))
      (else
       (count-trace a (cdr lat))))))

(define reduce
  (lambda (a lat)
    (cond
      ((null? lat) '())
      ((eq? (car lat) a) (reduce a (cdr lat)))
      (else
       (cons (car lat) (reduce a (cdr lat)))))))

(define revert
  (lambda (lst)
    (define revert-in
      (lambda (lat rev)
        (cond
          ((null? lat) rev)
          (else
           (revert-in (cdr lat) (cons (car lat) rev))))))
    (revert-in lst '())))

(define freq-*
  (lambda (lst func)
    (let freq-in ((lat lst) (seen '()))
      (cond
        ((null? lat) (revert seen))
        (else
         (let ((curr (car lat)))
           (freq-in (reduce curr lat)
                    (cons (list (func curr lat) curr) seen))))))))

(define count-freq
  (lambda (lat)
    (freq-* lat count-trace)))

;; (count-freq '(a a b c)) ;; '((2 a) (1 b) (1 c))
```

## Names Matter

In Lisp, if you do not know the special forms and the standard functions well, it is very
easy to redefine some of them by accident. People who write Lisp usually know several
dialects, such as Scheme, Common Lisp, or Clojure, and they carry their knowledge from one
to another. That is why nobody would use the name `reduce` for a function that removes an
element from a list. For reference, this is what
[`reduce`](https://www.lispworks.com/documentation/HyperSpec/Body/f_reduce.htm) does in
Common Lisp:

```lisp
(reduce #'* '(1 2 3 4 5)) ; => 120
```

It reduces the list to one value by applying the given function. So the right name is
[`remove`](https://www.lispworks.com/documentation/HyperSpec/Body/f_rm_rm.htm), and I keep
the same order of parameters:

```scheme
(define (remove elm ls)
  (cond
    ((null? ls) '())
    ((equal? (car ls) elm) (remove elm (cdr ls)))
    (else
     (cons (car ls) (remove elm (cdr ls))))))

;; (remove 'a '(a b c a)) ;; '(b c)
```

> **Note**
>
> Racket already has a
> [`remove`](https://docs.racket-lang.org/reference/pairs.html#%28def._%28%28lib._racket%2Fprivate%2Flist..rkt%29._remove%29%29),
> and our definition hides it. Be careful: Racket's `remove` deletes only the first
> match, so `(remove 'a '(a b a))` gives `'(b a)`. To delete all matches, Racket has
> `remove*`, which takes a list of values: `(remove* '(a) '(a b a))` gives `'(b)`. Our
> `remove` deletes all matches, as in Common Lisp.
{: .callout .callout--note}

## Reversing a List

So far, this is a small problem. But why did I write `revert`? Isn't `reverse` a better
name?

```scheme
(define (reverse seq)
  (if (null? seq)
      '()
      (cons (reverse (cdr seq)) (car seq))))

;; (reverse '(1 2 3 4)) ;; '((((() . 4) . 3) . 2) . 1)
```

`reverse` is definitely a better name. Also, influenced by *The Little Schemer*, I used
`cond` everywhere. That is not needed: for two branches, `if` is enough. Otherwise, my
reasoning was right. To reverse `(1 2 3 4)`:

> Put `(2 3 4)`, reversed, in front of `1`. Put `(3 4)`, reversed, in front of `(2 1)`.
> Put `4` in front of `(3 2 1)`.

In Part 1 I also noticed this:

> I always pass the `cdr` as the first argument, and let the second one build the result
> with `cons`.

My mistake is that I did not apply the first idea correctly. *Put ... in front of ...*,
again and again: this means recursion, and **at the end** I join the parts with `cons`.
So this:

```scheme
(cons (reverse (cdr seq)) (car seq))
```

follows my reasoning much better than this:

```scheme
(revert-in (cdr lat) (cons (car lat) rev))
```

Yes, but I was not careful! Don't ask me why I put the recursive call first in `cons`: it
breaks the golden rule of `cons` from Part 1, because the second argument is an atom, not
a list. Still, the result `((((() . 4) . 3) . 2) . 1)` is not that discouraging. I only
join the parts in the wrong way:

```scheme
(define (reverse items)
  (if (null? items)
      '()
      (append (reverse (cdr items))
              (list (car items)))))

;; (reverse '(1 2 3 4)) ;; '(4 3 2 1)
```

The trick here is `(list (car items))`. `append` takes two lists, so we write `4` as
`(4)`, and `append` collects the pieces into one list. This trick is used very often.

> **What it costs**
>
> This version follows the reasoning, but it is slow. `append` copies its whole first
> argument, so reversing a list of n elements takes O(n²) steps. The accumulator version
> `revert-in` from Part 1 takes O(n) steps and is tail-recursive. This is how `reverse` is
> usually written, and Scheme already has it built in; we write our own only to see how it
> works. But as you will see below, the final solution does not need to reverse anything.
{: .callout .callout--note}

## A Natural Recursion

Something similar happened with `freq-*`. Again the solution was in front of me, but I
missed it. This is the algorithm I proposed:

> Take the first atom and count how many times it appears. Write the result down, and
> reduce the list by removing this atom.

This is not very "suitable" for recursion, or at least it is not very intuitive. There
must always be a base case and then a recursion. The right way to say it is:

> Take the first atom, count how many times it appears, and write the result down. Repeat
> for the reduced list.

**Step 1.** Take the first atom and count how many times it appears:

```scheme
;; PARTIAL: only once
(define (trace-first seq)
  (list (count-trace (car seq) seq) (car seq)))

;; (trace-first '(a b c a)) ;; '(2 a)
```

**Step 2.** Repeat for the reduced list. "Repeat" tells us to use recursion:

```scheme
(define (trace-all seq)
  (if (null? seq)
      '()
      (cons (trace-first seq)
            (trace-all (remove (car seq) seq)))))

;; (trace-all '(a a a c b e f c f g t t w w w w w g h))
;; '((3 a) (2 c) (1 b) (1 e) (2 f) (2 g) (2 t) (5 w) (1 h))
```

You may not believe it, but that is all. Each result is consed onto the natural
recursion, as the Third Commandment from Part 1 says. So there is no accumulator and
nothing to reverse: the order is right from the start.

## Polishing

Now a little polishing:

```scheme
(define (trace-all seq)
  (letrec
      ((count-atom
        (lambda (a lat)
          (cond
            ((null? lat) 0)
            ((eq? a (car lat)) (+ 1 (count-atom a (cdr lat))))
            (else
             (count-atom a (cdr lat)))))))
    (if (null? seq)
        '()
        (let ((atom (car seq)))
          (cons (list (count-atom atom seq) atom)
                (trace-all (remove atom seq)))))))

;; (trace-all '(a a a c b e f c f g t t w w w w w g h))
;; '((3 a) (2 c) (1 b) (1 e) (2 f) (2 g) (2 t) (5 w) (1 h))
```

And a bit more for the final result. In Part 1 I passed the counting function as a
parameter, so that we can also count in other ways. I keep that, and I hide the loop
itself:

```scheme
(define (remove elm ls)
  (cond
    ((null? ls) '())
    ((equal? (car ls) elm) (remove elm (cdr ls)))
    (else
     (cons (car ls) (remove elm (cdr ls))))))

(define (freq-* seq counter)
  (letrec
      ((freq
        (lambda (lat)
          (if (null? lat)
              '()
              (let ((atom (car lat)))
                (cons (list (counter atom lat) atom)
                      (freq (remove atom lat))))))))
    (freq seq)))

(define (count-freq seq)
  (freq-* seq count-trace))

;; (count-freq '(a a a c b e f c f g t t w w w w w g h))
;; '((3 a) (2 c) (1 b) (1 e) (2 f) (2 g) (2 t) (5 w) (1 h))
```

The solution is now much cleaner and more professional. `remove` can stay a separate
function, because other functions will probably use it too. The counting function comes
as a parameter. The loop `freq` is different: it is a helper that is too specialized to
stand on its own. So I use this rule from
[The Seasoned Schemer](https://mitpress.mit.edu/9780262561006/the-seasoned-schemer/):

> **The Thirteenth Commandment**
>
> Use `(letrec ...)` to hide and to protect functions.
{: .callout .callout--commandment}

## Learning More

With this, I close the task. The result is quite good Scheme code.

Later, a reader pointed me to
[Roundabout](https://www.cs.uni.edu/~wallingf/patterns/recursion.html), a pattern
language for recursive programming by Eugene Wallingford, with examples in Scheme. It
gives names to the kinds of recursion in this task. `trace-all` is a *Structural
Recursion*: one case for each form of the list. `revert` and `revert-in` from Part 1 are
an *Interface Procedure* with an *Accumulator Variable*. And `freq`, hidden in `letrec`,
is a *Local Procedure*: it uses `counter` from `freq-*` directly, instead of passing it
on every call.

Here is a
[collection of lectures](https://www.youtube.com/watch?v=Ps8jOj7diA0&list=PL9D558D49CA734A02) that I
find very useful: Programming Paradigms (CS107) from Stanford, by Jerry Cain. For Scheme,
watch lectures 19 to 23. You will see how important `map` and `apply` are. Here is an
example from there:

```scheme
;;
;; Function: flatten-list
;; ----------------------
;; Flattens a list just like the original flatten does, but
;; it uses map, apply, and append instead of exposed car-cdr recursion.
;;
(define (flatten-list ls)
  (cond ((null? ls) '())
        ((not (list? ls)) (list ls))
        (else (apply append (map flatten-list ls)))))

;; (flatten-list '(1 2 (3) 4)) ;; '(1 2 3 4)
```

Not that hard, is it?
