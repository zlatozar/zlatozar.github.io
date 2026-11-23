---
layout: post
title: "Why Warnier/Orr Thinking Still Matters"
subtitle: "Start from the output. A 1970s design method that fits inside DDD and Hexagonal Architecture and speaks the language of functional programming."
description: "Warnier/Orr design revisited with Kirk Hansen's book: output-first thinking, six passes with modern names, validation at the boundary, and how it fits DDD, Hexagonal Architecture, and functional Python."
date: 2026-11-22 10:00:00 +0200
categories: [Architecture]
tags: [warnier-orr, ddd, hexagonal-architecture, functional-programming, python, design]
example: warnier-orr-diagrams
---

A ticket lands on your board:

> *Monthly sales report. For each store, list every sale and return, then the store's net
> total. End with a grand total. Finance closes the month with it.*
>
> ```text
> $ sales-report 2026-11 stores.csv sales.csv
> ```
>
> *The arguments are the month, the list of all stores, and the month's sales.*

Where do you start? Most of us open the CSV file and write a loop. Then the questions
arrive one by one. Is a store with no sales an error? What if a row has a broken amount?
Where does the formatting go? How many classes do we need?

DDD helps us find the bounded context and its language. Hexagonal Architecture keeps files
and databases out of the core. But both say little about the shape of the code *inside*
one use case: which types, which functions, which loop sits inside which, and where each
check belongs.

Jean-Dominique Warnier and Ken Orr answered this question in the 1970s: **start from the
output**. The output gives the shape of the data, and the data gives the structure of a
program. Kirk Hansen puts it in three rules. A *sequence* (different parts, each once, in
a fixed order) becomes statements one after another. A *selection* (one part or another)
becomes an `if`. This post applies these rules on purpose, with modern Python and contemporary
names.

---

## Where Warnier/Orr Fits

Warnier/Orr is not a rival of DDD or Hexagonal Architecture. It works at a finer zoom:

```text
Bounded Context     Which model? Which words?                DDD (strategic)
└─ Hexagon          What is core? What is an adapter?        Hexagonal Architecture
   └─ One use case  Which types, functions, loops, checks?   Warnier/Orr
```

DDD and Hexagonal Architecture draw the big boxes. Warnier/Orr fills one of them, from the
raw input at one edge to the finished output at the other. It also shares the core ideas
of functional programming: data first, pure functions, no shared state. Every block schema
(programming logic) could be represented by 

## The Mindset: Five Questions

You do not have to draw diagrams to use Warnier/Orr. You have to ask five questions
before you write code:

1. **What comes out?** Write a sample of the output by hand, with real values. Include
   the bad case: what does the user see when the input is wrong?
2. **How is it nested?** For each item on the sample, ask how often it is printed. The
   grand total is printed once per report, the net total once per store, and the amount
   once per transaction. Report, store, and transaction are the levels. Each store sits
   inside the report, and each transaction sits inside a store. That nesting is the
   hierarchy.
3. **How many?** Optional `(0,1)`, zero or more `(0,n)`, or at least one `(1,n)`? Each
   answer is a rule, and later a check.
4. **Which excludes which?** A transaction is a sale *or* a return, never both. Warnier
   writes `⊕`; today we say *sum type*.
5. **Logical or physical?** The logical part is what the report says: the net total of
   store S1 is 100.00. The physical part is how it is shown: page breaks, column widths,
   JSON field names. Switch from a printed page to JSON, and only the physical part
   changes. Keep them apart; the physical part lives at the edges.

Each answer splits the problem into smaller parts, and each part gets its own type and
function. That is divide and conquer, guided by the data.

## Six Passes, Classic and Modern Names

The classic acronyms sound like 1970s mainframe jargon, because they are. Rename them
freely; the ideas survive. Each pass answers one question:

1. **Define the result** (LOS, built with LFUD). What comes out, and how is it nested?
2. **Derive the input** (LIF). What must come in?
3. **Design the process** (LPS). What happens before, for each, and after?
4. **Parse the input** (LRR). How does untrusted data become valid input?
5. **Format the output** (PPR). How does the result look?
6. **Verify.** Where is each rule checked?

The classic names: *Logical Output Structure* (LOS), built by *List, Frequency,
Universals, Draw* (LFUD), and called *Logical Output File* (LOF) by Warnier; *Logical
Input File* (LIF); *Logical Process Structure* (LPS); *Logical Read Routine* (LRR);
*Physical Put Routine* (PPR).

Each pass also has its place in the code:

```text
          inbound              core             outbound
          adapter                               adapter
input ──▶ 4. Parse ──▶ 1. Define the result ──▶ 5. Format ──▶ output
                       2. Derive the input
                       3. Design the process
          └────── 6. Verify: types, parser, tests ──────┘
```

To remember the modern names: three **D**s for the core, two adapters at the edges, one
check across all of them. The order is ours, not Hansen's. He formats the output before
he parses the input; we parse first, because that is how the data flows.

## Walkthrough: The Monthly Sales Report

Let us take the ticket through all six passes, in Python 3.12+.

### Pass 1: Define the Result

First the sample output: not a class diagram, but the page that finance will read.

```text
=== SALES REPORT 2026-11 ===

S1 Sofia Central
  T1  SALE      +120.00
  T2  RETURN     -20.00
  net total      100.00

S2 Plovdiv North
  (no transactions)
  net total        0.00

GRAND TOTAL      100.00
```

Then the bad case: what if some rows in the file are wrong? We could skip them and print
totals from the rest. But such a total looks correct and is not, and finance may trust
it. No total is safer than a wrong one. So if even one row is bad, the report prints no
totals. Instead, it lists *every* bad row, not only the first, so the user can fix the
whole file at once.

```text
REJECTED: 3 bad row(s), no totals printed
  row 2: amount 'abc' is not a number
  row 3: unknown store 'S9'
  row 4: amount '-5.00' must be a positive number
```

Now LFUD. **List** the atoms, the smallest items on the samples: the period, the store
id and name, the amount, and the totals. Give the **frequency** of each by asking "once
per what?": the grand total once per report, the net total once per store, the amount
once per transaction. The answers (report, store, transaction) are the **universals**:
the groups that the atoms belong to. Each universal becomes a bracket that holds its
atoms. Then **draw**, with exactly one place for each item:

```text
                        ┌ period
                        │
                        │        ┌ store id
                        │        │ name
                        │        │
                        │        │              ┌ Sale (0,1)
              ┌ Report ─┤ Store ─┤ Transaction ─┤   ⊕
              │ (0,1)   │ (1,n)  │ (0,n)        └ Return (0,1)
              │         │        │
Sales output ─┤         │        └ net total
              │         │
              │   ⊕     └ grand total
              │
              │                            ┌ row number
              └ Rejection ── Rejected row ─┤
                (0,1)        (1,n)         └ reason
```

A sales output is a report *or* a rejection. Errors are output too: someone reads the
rejection, just like the report, so it is designed in pass 1 with the same care, not
added later when the parser finds bad rows. The drawing also fixes two rules: one bad
row means no totals at all, and a rejection lists every bad row (1,n), not only the
first one.

Each bracket becomes a type, repetition becomes a tuple, and `⊕` becomes a union, named
with the `type` statement:

```python
from dataclasses import dataclass
from decimal import Decimal
from typing import ClassVar

ZERO = Decimal("0.00")


@dataclass(frozen=True)
class Sale:
    tx_id: str
    amount: Decimal  # always > 0; the kind decides the sign
    label: ClassVar[str] = "SALE"


@dataclass(frozen=True)
class Return:
    tx_id: str
    amount: Decimal
    label: ClassVar[str] = "RETURN"


type Transaction = Sale | Return  # ⊕: a sale or a return, never both


@dataclass(frozen=True)
class StoreSection:
    store_id: str
    name: str
    transactions: tuple[Transaction, ...]  # (0,n): a quiet store is valid
    net_total: Decimal


@dataclass(frozen=True)
class Report:
    period: str
    stores: tuple[StoreSection, ...]  # (1,n)
    grand_total: Decimal


@dataclass(frozen=True)
class RejectedRow:
    number: int
    reason: str


@dataclass(frozen=True)
class Rejection:
    rows: tuple[RejectedRow, ...]  # (1,n)


type SalesOutput = Report | Rejection  # ⊕: totals or reasons, never both
```

### Pass 2: Derive the Input

Hansen's rule for the ideal input: take the output and drop everything you can
calculate, plus fixed text such as titles. What remains is *required*. The hierarchy
stays the same:

```python
@dataclass(frozen=True)
class StoreInput:
    store_id: str
    name: str
    transactions: tuple[Transaction, ...]  # (0,n)


@dataclass(frozen=True)
class SalesInput:
    period: str
    stores: tuple[StoreInput, ...]  # (1,n)

    def __post_init__(self) -> None:
        if not self.stores:  # a broken setup, not bad data: fail fast
            raise ValueError("SalesInput needs at least one store (1,n)")
```

The totals are gone, because the program calculates them. `Sale` and `Return` have
nothing to calculate, so the input reuses them as they are.

The list of stores does not come from the CSV file. It comes from the *store master*
(`stores.csv` in the ticket), a setup list of all stores, so a store with no sales still
appears in the report. If that list is empty, the user's file is not to blame; the
program was set up wrong. So we do not return a `Rejection`; we raise an error and stop
at once (*fail fast*).

The real file is not a tree like `SalesInput`. It is a flat list of CSV rows, all
strings. Turning those rows into `SalesInput` is the job of pass 4.

### Pass 3: Design the Process

Now add **Begin** and **End** to every repeating level: the title comes before the stores
(*Begin Report*), the grand total after them (*End Report*). Do not print yet. This pass
only computes the totals and builds the report as a value; pass 5 prints it. In code,
Begin, Process, End is often a fold:

```python
from typing import assert_never


def signed(tx: Transaction) -> Decimal:
    """A sale adds to the total; a return takes away from it."""
    match tx:
        case Sale(amount=amount):
            return amount
        case Return(amount=amount):
            return -amount
        case _:
            assert_never(tx)


def build_store(store: StoreInput) -> StoreSection:
    net_total = sum((signed(tx) for tx in store.transactions), ZERO)
    return StoreSection(store.store_id, store.name, store.transactions, net_total)


def build_report(sales: SalesInput) -> Report:
    stores = tuple(build_store(store) for store in sales.stores)
    grand_total = sum((store.net_total for store in stores), ZERO)
    return Report(sales.period, stores, grand_total)
```

`sum(..., ZERO)` is the pattern in one line: `ZERO` is Begin, adding each amount is
Process, the result is End. The `match` in `signed` is exhaustive: it covers every case of
`Transaction`, the `⊕` from the diagram, and the last case makes a type checker prove it.
When every kind is covered, nothing is left for `case _`, and `assert_never` accepts only
that: nothing (the type `Never`). Add a third kind to the union, and the checker reports
that it reaches `assert_never`. End every `match` over a `⊕` this way. Without it, a loop
or a generator that misses a case gives no error at all; it just skips the item. The core
is done: one pure function per bracket, no I/O.

### Pass 4: Parse the Input

Hansen calls this the brick-wall rule: "build a brick wall around your output; then you
only worry about calculations inside the wall". All contact with the outside world goes
through the read routine, which today we call an inbound adapter. The rule has a modern
name too, *parse, don't validate*: turn a raw row into a type that cannot be wrong.

```python
from collections.abc import Iterable
from decimal import InvalidOperation

KINDS = {kind.label: kind for kind in (Sale, Return)}  # {"SALE": Sale, "RETURN": Return}


class RowError(ValueError):
    """One raw row that cannot become a Transaction."""


def read_input(
    period: str, stores: dict[str, str], rows: Iterable[list[str]]
) -> SalesInput | Rejection:
    """Raw CSV rows in; valid input, or every reason why not, out."""
    grouped: dict[str, list[Transaction]] = {store_id: [] for store_id in stores}
    rejected: list[RejectedRow] = []
    for number, fields in enumerate(rows, start=1):
        try:
            store_id, tx = parse_row(fields, stores)
        except RowError as error:
            rejected.append(RejectedRow(number, str(error)))
            continue
        grouped[store_id].append(tx)
    if rejected:
        return Rejection(tuple(rejected))
    return SalesInput(period, tuple(
        StoreInput(store_id, name, tuple(grouped[store_id])) for store_id, name in stores.items()
    ))


def parse_row(fields: list[str], stores: dict[str, str]) -> tuple[str, Transaction]:
    if len(fields) != 4:
        raise RowError(f"expected 4 fields, got {len(fields)}")
    tx_id, store_id, kind, amount = (field.strip() for field in fields)
    if store_id not in stores:
        raise RowError(f"unknown store {store_id!r}")
    if kind not in KINDS:
        raise RowError(f"unknown kind {kind!r}")
    return store_id, KINDS[kind](tx_id, parse_amount(amount))


def parse_amount(text: str) -> Decimal:
    try:
        amount = Decimal(text)
    except InvalidOperation:
        raise RowError(f"amount {text!r} is not a number") from None
    if not amount.is_finite() or amount <= 0:  # Decimal accepts "NaN" and "Infinity"
        raise RowError(f"amount {text!r} must be a positive number")
    return amount
```

Three choices are deliberate. Every row is parsed, so the user fixes the whole file in
one round. Each error carries its row number. And bad rows are expected, so they come
back as a value, `SalesInput | Rejection`; `RowError` never leaves this adapter. The
store master (`{id: name}`) drives the grouping, which is how the quiet store `S2`
reaches the report.

### Pass 5: Format the Output

The physical output walks the same tree, and `match` makes the top-level `⊕` explicit:

```python
def format_output(output: SalesOutput) -> str:
    match output:
        case Rejection(rows=rows):
            lines = [f"REJECTED: {len(rows)} bad row(s), no totals printed"]
            lines += [f"  row {bad.number}: {bad.reason}" for bad in rows]
        case Report():
            lines = [f"=== SALES REPORT {output.period} ==="]  # Begin Report
            for store in output.stores:  # Store (1,n)
                lines.append(f"\n{store.store_id} {store.name}")  # Begin Store
                if not store.transactions:
                    lines.append("  (no transactions)")
                for tx in store.transactions:  # Transaction (0,n)
                    lines.append(f"  {tx.tx_id:<4}{tx.label:<8}{signed(tx):>+9.2f}")
                lines.append(f"  net total   {store.net_total:>9.2f}")  # End Store
            lines.append(f"\nGRAND TOTAL   {output.grand_total:>9.2f}")  # End Report
        case _:
            assert_never(output)
    return "\n".join(lines)
```

Pages, CSV, JSON, or HTML belong here; the core does not change. The wiring is one more
`match`, over the result of `read_input`:

```python
import csv
import sys
from pathlib import Path


def run(period: str, stores: dict[str, str], raw_csv: str) -> str:
    parsed = read_input(period, stores, csv.reader(raw_csv.splitlines()))
    match parsed:
        case Rejection():
            return format_output(parsed)
        case SalesInput():
            return format_output(build_report(parsed))
        case _:
            assert_never(parsed)


def main() -> None:
    """sales-report 2026-11 stores.csv sales.csv"""
    period, stores_file, sales_file = sys.argv[1:]
    store_rows = Path(stores_file).read_text().splitlines()  # "S1,Sofia Central"
    stores = dict(csv.reader(store_rows))
    print(run(period, stores, Path(sales_file).read_text()))
```

`main` only reads files and prints. `run` takes plain strings, so tests can call it
without files.

Scott Wlaschin's [railway-oriented programming][rop] fits here too: each `case` is a
track, success or failure, and both tracks end in `format_output`.

### Pass 6: Verify

Every mark in the diagram is a rule, and every rule needs a place where it is checked.
The language construct you choose for a mark decides how early that check happens:

| Mark | Python | Other languages | Checked by |
| --- | --- | --- | --- |
| Sequence | frozen `dataclass` | record, struct | type checker |
| `(0,1)` | <code>X &#124; None</code> | `Option`, nullable type | type checker |
| `(0,n)` | `tuple[X, ...]` | list | type checker |
| `(1,n)` | `tuple` + check in `__post_init__` | non-empty list, or a constructor check | constructor, at run time |
| `⊕` | <code>type T = A &#124; B</code> + `match` ending in `assert_never` | sum type, sealed class | type checker |
| Value rule | parse function | parse function, value type | parser, at the boundary |
| Page layout | — | — | golden test |

The rows run from the earliest check to the latest. In the sales report, the type checker
covers the brackets and both `⊕`. `SalesInput.__post_init__` checks that there is at
least one store, `parse_amount` checks that an amount is positive, and the golden test
below checks the page.

Two kinds of failure: bad data is expected, so collect it and report it as output; a
broken invariant in the core is a bug, so fail fast.

These checks cover the rules one by one. The samples from pass 1 check that the parts work
together. We wrote them by hand, before any code. Now we feed in the input that should
produce them, run the whole program, and compare the full text:

```python
STORES = {"S1": "Sofia Central", "S2": "Plovdiv North"}

GOOD_CSV = """\
T1,S1,SALE,120.00
T2,S1,RETURN,20.00
"""

BAD_CSV = """\
T1,S1,SALE,120.00
T2,S1,SALE,abc
T3,S9,SALE,10.00
T4,S1,RETURN,-5.00
"""

assert run("2026-11", STORES, GOOD_CSV) == SAMPLE_REPORT
assert run("2026-11", STORES, BAD_CSV) == SAMPLE_REJECTION
```

`SAMPLE_REPORT` and `SAMPLE_REJECTION` are the two texts from pass 1, copied as they are.
A test that compares the whole output with a saved text is a *golden test*. The first run
may fail because the hand-made sample is wrong: a column too narrow, a typo. Fix whichever
side is wrong, the code or the sample. After that the sample is frozen, and any change to
the report breaks the test. Two examples do not prove that the design is correct, but
they close the loop: the page you wrote first is the page you check last.

## Divide and Conquer: When Structures Clash

Sometimes two structures do not line up, and one loop cannot follow both. Jackson called
this a *structure clash*. Hansen follows his advice: do not force one loop to serve two
trees. **Use two programs joined by a stream, one per structure**. Jackson named three
kinds of clash: boundary, ordering, and interleaving. Each needs its own information in
the stream, and each has its own cost.

### Boundary Clash

A report is stores and transactions, but paper is pages, and a store may start on one
page and end on the next. In Python, the two programs become a generator pipeline:

```python
pages = paginate(lines_of(report), lines_per_page=54)  # a sketch
```

The stream needs more than text. When `lines_of` flattens the report into lines, the
store boundaries are lost, but some page rules still need them. If a store goes on to the
next page, that page should start with `S1 Sofia Central (continued)`. From plain text,
`paginate` cannot know which store it is in. So the first program adds meta information
to the stream: each line carries its store.

```python
@dataclass(frozen=True)
class ReportLine:
    text: str
    store: str | None  # meta: "S1 Sofia Central", or None outside a store
```

Find the meta information the same way as everything else: from the output. List the
rules of the second tree. Every rule that names a part of the first tree becomes a marker
or a field in the stream. Put it in the stream, not in the domain types: `StoreSection`
does not change.

The two programs are different kinds of function. The first flattens a tree into a
stream, and that is a *bind*: each node becomes its own lines, and the results are joined
in order. Here each store becomes its lines. In Python that is `yield from`; in other
languages it is often called `flatMap`. `paginate` cannot be a bind. Bind breaks one node
into lines, but it never puts lines of different nodes into one group, and that is
exactly what a page does. `paginate` needs state, the lines already on the page, so it is
an *unfold*. In Python, a generator with a local variable keeps that state.

So the whole pipeline has two steps: flatten one tree into a stream, then build the other
tree from that stream. The two steps run together, not one after the other. `paginate`
asks for the next line only when it needs one, so the first page is ready before the
whole report is flattened. Jackson called this *program inversion*: one program becomes
a subroutine that the other calls for each item, and it keeps its place between calls.
A Python generator works this way, with no extra library.

Each `next()` goes down the pipeline, and one line comes back up:

```text
caller        paginate          lines_of          a store
  │ next() ────►│                 │                 │
  │             │ next() ────────►│ yield from ────►│
  │             │◄───────────── a line ─────────────│  lines_of and a store pause
  │             │ ... until the page is full
  │◄─── Page ───│
```

Each generator on the path pauses at its `yield` and keeps its local variables. The next
call resumes it there. The total work does not change; only its order does. But the
first page now costs only the work for one page, whatever the size of the input. A
pipeline that builds a full list at each step must process the whole input before it
can return page 1.

The companion project `PrintersDevil` does this at full scale: a text formatter from
Charles Wetherell's [*Etudes for Programmers*][wetherell-etudes], where paragraphs clash
with pages. Its line stream carries two markers next to the text: `GapLine`, the space
before a paragraph, dropped at the top of a page, and `PageBreak`, which can change the
page height.

### Ordering Clash

You have already met one, in pass 4. The CSV file lists the rows as they were recorded,
with all stores mixed. The report needs them store by store, in the order of the store
master. Nothing in the file marks the end of a store: the last row of the file may still
belong to the first store. So no store is complete before the whole file is read.

The store id in every row is the key: `read_input` puts each row into the list of its
store, and the store master gives the order of the stores. The stream needs nothing more.

The cost is waiting. The two programs cannot run together; the grouping stands between
them. A month of sales fits in memory, and a dict, or `sorted` followed by
`itertools.groupby`, is enough. When the input is too large for memory, sort it outside
the program: with an external sort, or with `ORDER BY` in the database.

### Interleaving Clash

A server log holds the lines of many user sessions, mixed as they happen. This time each
group marks its own end. A session has its own structure: a login, (0,n) actions, and a
logout. That is Begin, Process, and End from pass 3, but many sessions are open at the
same time. A session is complete at its logout, long before the log ends, so its summary
can come out at once. The summaries come out in the order the sessions end, not in the
order of a key.

Jackson's answer is one small program per session, paused between its lines. In Python,
a dict keeps the state of each open session, and the session id in every line is the
group key:

```python
@dataclass
class OpenSession:
    started: datetime
    actions: int = 0


def session_summaries(log: Iterable[LogLine]) -> Iterator[SessionSummary]:  # a sketch
    open_sessions: dict[str, OpenSession] = {}
    for line in log:
        match line:
            case Login(session_id=sid, at=at):  # Begin
                open_sessions[sid] = OpenSession(started=at)
            case Action(session_id=sid):  # Process
                open_sessions[sid].actions += 1
            case Logout(session_id=sid, at=at):  # End
                session = open_sessions.pop(sid)
                yield SessionSummary(sid, session.started, at, session.actions)
            case _:
                assert_never(line)
```

The two programs run together again, but now you must know how each group ends. Logout
is the easy case. A user who closes the browser never logs out, so the program needs a
timeout. And when the log stops, some sessions are still open: report them as
incomplete, or drop them on purpose. Without such rules, the dict only grows.

### Which Clash Is It?

Here is a simple test. When you turn one structure into another, try to write the step
as a *bind*. If you can, the structures fit. If you cannot, because an output group needs
items from more than one input node, the structures clash, and the step needs its own
program. Then look at the output group:

* It cuts across the input groups, like a page that holds the end of one store and the
  start of the next: a **boundary clash**. The stream carries markers.
* It needs the input items in another order, like rows by store instead of by time: an
  **ordering clash**. The stream carries a sort key.
* It collects its items while other groups are still open, like the lines of one session
  between login and logout: an **interleaving clash**. The stream carries a group key.

Ordering and interleaving look alike: in both, items of many groups come mixed. One
question tells them apart: can a group be finished before the input ends?

* No. Nothing marks the end of a group, so you read the whole input, and the groups come
  out in key order. That is an ordering clash, like the sales file.
* Yes. Each group has its own Begin and End, so it comes out as soon as it ends, and the
  groups come out in the order they end. That is an interleaving clash, like the
  sessions.

The border is also a design choice. Any interleaving clash can be solved as an ordering
clash: read the whole log, sort it by session id, then group. Do that when you can wait
for the end of the input, or when the output needs key order: a session report sorted by
user is an ordering clash, even with logout lines. Keep one state per open group only
when you cannot wait: the log never stops, or each summary is needed as soon as its
session ends.

## A Way of Thinking, Not a New Method

Nothing in this post is new. Warnier/Orr is not a framework, and you do not need its
diagrams. It is a way of thinking at the level of logical construction: before you write
code, decide what comes out, how it is nested, how many of each part there are, and which
part excludes which. The types, the functions, and the checks follow from the answers.

The construction needs names, and the programmer does not invent them. They come from the
domain, and DDD is how we find them: the bounded context and its ubiquitous language.
Report, store, transaction, sale, and return are words that finance uses. The same words
name the levels of the diagram, the types in the core, and the functions that build them.

The same line of reasoning runs through the whole hexagon. It starts at the output and
works back to the input; the data later flows the other way. The numbers are the passes:

```text
┌─ Bounded context: report, store, transaction, sale, return ──┐
│  reasoning: from the output back to the input                │
│  ◄─────────────────────────────────────────────────────────  │
│                                                              │
│  inbound          core                         outbound      │
│  adapter                                       adapter       │
│  (physical)       (logical)                    (physical)    │
│  ┌──────────┐     ┌──────────────────────┐     ┌──────────┐  │
│  │ CSV rows │ ──► │ SalesInput ─► Report │ ──► │ page     │  │
│  │ parse  4 │     │ 2           3      1 │     │ format 5 │  │
│  └──────────┘     └──────────────────────┘     └──────────┘  │
│                                                              │
│  ─────────────────────────────────────────────────────────►  │
│  data: from the input to the output                          │
└──────────────────────────────────────────────────────────────┘
```

The core holds the logical structures, named in domain words. The adapters hold the
physical ones: CSV rows on one side, the printed page on the other. They are structures
too, and the same questions apply: how is it nested, how many, which excludes which.
Where a physical structure differs from the logical one, the adapter translates. Where
the two do not line up at all, it is a structure clash, and it takes two programs joined
by a stream. Pass 6 closes the loop: the page that comes out is checked against the
sample you wrote first.

## Beyond Reports

The approach works wherever the problem is shaped by data with a clear structure: reports,
file formats, batch jobs, data moved between systems, parsers, API payloads, documents,
and screens. In each of them something comes out with a shape you can draw, and the
program follows that shape.

It helps less when the hard part lies somewhere else:

* **Events over time.** In a GUI, a workflow, or a device controller, the structure is the
  order in which things happen, not a file. The five questions still apply to that order,
  but the diagram is no longer the whole design.
* **Algorithms.** In search, optimization, or numerical code the output is often a single
  number. The work is in the computation, and the shape of the output tells you little.
* **Concurrency and distribution.** Message order, partial failure, and consistency are
  the hard parts. Structure helps with the messages, not with the timing.
* **Exploratory work.** If nobody knows yet what should come out, you cannot design from
  it. Build a prototype first, then draw the structure.

## Techniques to Explore

Each technique below takes one idea from this post further.

* **Specification by example and golden tests** (passes 1 and 6). Agree on concrete
  samples before writing code, then keep them as tests.
* **Algebraic data types** (passes 1 and 2). Product types for sequence and sum types for
  `⊕` put the diagram into the type system; an exhaustive `match` lets the type checker
  enforce it.
* **Structural recursion** (pass 3). The code follows the shape of the data. A choice
  (`⊕`) becomes a `match` with one case per alternative, and each part gets its own call.
  Pass 3 already does this: `build_report` calls `build_store`, which calls `signed`. It
  becomes recursion when the data contains itself, such as a folder inside a folder: the
  function calls itself on the inner one. The parts get smaller, so it always ends.
* **Parse, don't validate** (pass 4). Turn raw input into types that cannot hold invalid
  data. In Python, Pydantic or a small constructor per type does this.
* **Railway-oriented programming** (passes 4 and 5). Functions return a value *or* an
  error, and an error skips the rest of the work.
* **Generators and stream pipelines** (structure clash). The two programs of a clash
  become two generators, joined the way a Unix pipe joins two commands. `itertools` has
  most of the plumbing.
* **Property-based testing** (pass 6). Tools such as Hypothesis generate inputs from a
  description of the data; the counts in the diagram, (0,1) and (1,n), become the rules.
* **Contract-first APIs.** For a service, the output is the response. Write the OpenAPI
  or JSON Schema contract first, then the code behind it.

## From Theory to Tool

This post is the theory, and the companion project is the practice. The third part is a
tool: an [Agent Skill][wo-skill] that gives the same steps to a coding agent. Before it
writes code, the agent writes the output sample and both trees, and checks for a
structure clash. It stops twice for your approval: after the sample, and when it finds
a clash. The skill decides only the structure inside one use case. The names come from
your domain, and the architecture and style come from your project. The companion README
shows how to install it.

---

## Bibliography

* Kirk Hansen, [*Data Structured Program Design*][hansen-dspd] (Ken Orr & Associates,
  1983; Prentice Hall, 1986). The source of the classic names used here.
* Michael A. Jackson, [*Principles of Program Design*][jackson-ppd] (Academic Press,
  1975).
* Charles Wetherell, [*Etudes for Programmers*][wetherell-etudes] (Prentice Hall, 1978).

[hansen-dspd]: https://www.amazon.com/Data-Structured-Program-Design-Hansen/dp/0131968416
[jackson-ppd]: https://www.amazon.com/Principles-Program-Design-APIC-Jackson/dp/0123790506
[wetherell-etudes]: https://www.amazon.com/Etudes-Programmers-Charles-Wetherell/dp/0132918072
[wo-skill]: https://github.com/zlatozar/zlatozar.github.io/tree/main/examples/warnier-orr-diagrams/skills/warnier-orr-design
