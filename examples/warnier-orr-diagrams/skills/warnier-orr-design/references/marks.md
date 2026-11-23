# Marks and Language Constructs

Every mark in the output tree is a rule, and every rule needs a place where it is
checked. The construct you choose decides how early that check happens. Choose the
earliest one the language offers: type, then constructor, then parser, then test.

| Mark | Python | TypeScript | Java | Kotlin | Rust |
| --- | --- | --- | --- | --- | --- |
| Sequence | frozen `dataclass` | object type with `readonly` fields | `record` | `data class` | `struct` |
| `(0,1)` | `X \| None` | `X \| undefined` | `Optional<X>` | `X?` | `Option<X>` |
| `(0,n)` | `tuple[X, ...]` | `readonly X[]` | `List<X>` | `List<X>` | `Vec<X>` |
| `(1,n)` | `tuple` + check in `__post_init__` | `readonly [X, ...X[]]` | `List<X>` + check in the compact constructor | `List<X>` + `require` in `init` | `first: X` + `rest: Vec<X>`, or a check in `new` |
| `⊕` | `A \| B` + `match` with `assert_never` | union with a `kind` tag + `never` check | `sealed interface` + `switch` with patterns | `sealed interface` + `when` | `enum` + `match` |
| Value rule | parse function | parse function + branded type | static factory | factory function | `TryFrom` or `new` returning `Result` |
| Page layout | golden test | golden test | golden test | golden test | golden test |

Who checks each row:

* **Sequence, `(0,1)`, `(0,n)`, `⊕`:** the compiler or the type checker, in all five
  languages. In Python, run a type checker such as ty, mypy, or pyright.
* **`(1,n)`:** the constructor, at run time. TypeScript's `[X, ...X[]]` and Rust's
  `first` + `rest` move this check to the compiler.
* **Value rule** (a positive amount, a known store id): the parser, at the edge. After
  that, the type says the value is valid, and the core does not check it again.
* **Page layout:** only a golden test sees it.

Two kinds of failure:

* Bad data is expected. Collect every bad item and return it as output, the bad case
  from phase 1.
* A broken setup or a broken invariant in the core is a bug. Fail fast.

## An Exhaustive Match

The match for `⊕` must fail to type-check when a new alternative is added. In Python:

```python
from typing import assert_never  # standard since Python 3.11


def signed(tx: Transaction) -> Decimal:
    match tx:
        case Sale(amount=amount):
            return amount
        case Return(amount=amount):
            return -amount
        case _:
            assert_never(tx)
```

Java, Kotlin, and Rust check this without help, as long as there is no default branch.
In TypeScript, assign the value to a variable of type `never` in the default branch.
