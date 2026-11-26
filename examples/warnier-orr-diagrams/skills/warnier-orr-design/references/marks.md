# Marks and Language Constructs

Every mark in the output tree is a rule, and every rule needs a place where it is
checked. Check it as early as possible: type, then constructor, then parser, then test.

Choose a construct by the guarantee it gives, not by its name. The method needs only
these guarantees, so any language fits. Where a language gives a weaker guarantee, the
check moves one step later.

| Mark | The construct must | Checked by |
| --- | --- | --- |
| Sequence | have fixed fields that cannot change after it is built | type |
| `(0,1)` | make every reader handle the absent case | type |
| `(0,n)` | be a collection that cannot change | type |
| `(1,n)` | refuse an empty collection | constructor; type, if the language can say "not empty" |
| `⊕` | close the set of alternatives, and fail the build when a match misses one | type |
| Value rule | exist only after a parse at the edge | parser |
| Page layout | match a frozen sample of the output | golden test |

In a language with optional types, "type" means a type checker that runs in the build.

A value rule, such as a positive amount or a known store id, is checked once. After the
parser, the type says the value is valid, and the core does not check it again.

Two kinds of failure:

* Bad data is expected. Collect every bad item and return it as output, the bad case
  from phase 1.
* A broken setup or a broken invariant in the core is a bug. Fail fast.

## An Exhaustive Match

Adding an alternative to a `⊕` must break the build at every match that misses it. A
default branch that swallows the rest breaks this rule: the new case passes in silence.

In Python:

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

Most typed languages check this without help, as long as there is no default branch.
Where one does not, the default branch must fail to type-check, as `assert_never` does.
