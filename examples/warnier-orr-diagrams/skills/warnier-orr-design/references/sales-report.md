# Worked Example: The Monthly Sales Report

This is the example from the article [Why Warnier/Orr Thinking Still Matters][post],
written as the design note the skill produces before any code. The article has the full
Python code for each phase.

## The Ticket

> Monthly sales report. For each store, list every sale and return, then the store's net
> total. End with a grand total. Finance closes the month with it.
>
> ```text
> $ sales-report 2026-11 stores.csv sales.csv
> ```
>
> The arguments are the month, the list of all stores, and the month's sales.

## Design: Monthly Sales Report

**Output sample:**

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

**Bad case:** if even one row is bad, print no totals, and list every bad row, so the
user can fix the whole file at once. A total from the good rows only looks correct and
is not.

```text
REJECTED: 3 bad row(s), no totals printed
  row 2: amount 'abc' is not a number
  row 3: unknown store 'S9'
  row 4: amount '-5.00' must be a positive number
```

**Output tree:**

```text
Sales output
  Report (0,1)
    period
    Store (1,n)
      store id, name
      Transaction (0,n)
        Sale (0,1)
        ⊕
        Return (0,1)
      net total
    grand total
  ⊕
  Rejection (0,1)
    Rejected row (1,n)
      row number, reason
```

**Physical tree:** none. The report is one text with no pages, so the layout has no
groups of its own.

**Open questions** (answered by the ticket owner before code):

* Is a store with no sales an error? No: show it with `(no transactions)` and `0.00`.
* What if the store master is empty? The program was set up wrong: fail fast, do not
  print a rejection.
* Is a zero or negative amount valid? No: the kind (SALE or RETURN) gives the sign, so
  every amount must be positive.

**Input tree** (the output without the calculated items and the fixed text):

```text
Sales input
  period
  Store (1,n)
    store id, name
    Transaction (0,n)
      Sale (0,1)
      ⊕
      Return (0,1)
```

The real input is two flat files. The stores come from `stores.csv`, the store master, so
a store with no sales still appears. The transactions come from `sales.csv`, one row
each: `T1,S1,SALE,120.00`.

**Calculated:** net total per store, grand total.

**Clash check:**

* Real input against the output tree: the rows of one store are scattered through
  `sales.csv`, and nothing marks where a store ends. That is a grouping clash, with the
  store id as the group key. It needs no sort: the order of the stores comes from
  `stores.csv`, and the rows of each store keep their file order. A month of rows fits in
  memory, so the parser reads the file whole. It starts one empty bucket per store from
  `stores.csv`, puts each row into the bucket of its store id, then builds the input
  tree.
* Output tree against the physical tree: there is no physical tree, and each store
  becomes its own lines, so formatting is a bind. If finance later asks for pages of 54
  lines with a "continued" header, that is a boundary clash: pages cut across stores.
  See [clashes.md](clashes.md).

**New questions:** none.

## Rules and Their Checks

| Rule | Check |
| --- | --- |
| Store (1,n) | constructor of the sales input: fail fast |
| Transaction (0,n), Sale ⊕ Return | types, and an exhaustive match in `signed` |
| Report ⊕ Rejection | types, and an exhaustive match in the formatter |
| Amount is a positive number | parser: a bad row becomes a rejected row |
| Store id is known | parser: a bad row becomes a rejected row |
| Page layout | golden tests from both samples |

## The Phases in Code

1. **What comes out:** the output types `Report`, `StoreSection`, `Rejection`, and the
   union `SalesOutput = Report | Rejection`.
2. **What goes in:** `SalesInput` and `StoreInput`; `Sale` and `Return` are reused as
   they are.
3. **Core:** `signed`, `build_store`, and `build_report`, one pure function per level.
   The totals are `sum(..., ZERO)`, a fold.
4. **Parse:** `read_input` parses every row and returns `SalesInput | Rejection`.
5. **Format:** `format_output` is the only function that knows the layout.
6. **Verify:** two golden tests, one for each sample.

[post]: https://zlatozar.github.io/warnier-orr-diagrams/
