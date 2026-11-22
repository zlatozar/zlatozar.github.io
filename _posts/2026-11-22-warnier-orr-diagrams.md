# Why Warnier/Orr Thinking Still Matters (Even When Nobody Draws Bracket Trees)

Warnier/Orr diagrams grew out of 1970s structured programming and batch data processing. Jean-Dominique Warnier introduced the method in *Logical Construction of Programs* (LCP, 1974) and later broadened it to whole systems in *Logical Construction of Systems* (LCS, 1981). In the United States, Kenneth Orr extended and popularized the approach in *Structured Systems Development* (1977), which grew into his Data Structured Systems Development (DSSD) method. Instead of arrow-heavy flowcharts, the diagrams model data and program hierarchies with nested brackets, repetition bounds like `(0, n)` and `(1, n)`, and mutually exclusive choices (`⊕`).

As a day-to-day design method, classic Warnier/Orr has largely given way to object-oriented design, domain modeling, functional programming, and modern type systems. Yet the **mental habits** it enforces—designing from outputs backward, making cardinality and exclusivity explicit, and letting data hierarchy dictate control flow—remain remarkably effective today.

---

## The Core Ideas Behind Warnier/Orr

* **Output-first thinking:** Define what the system must produce before designing loops, branches, or input parsing.
* **Explicit cardinality:** Distinguish `(0, 1)` (optional), `(0, n)` (zero or many—an empty collection is valid), and `(1, n)` (at least one required) so boundary cases aren't afterthoughts.
* **Mutually exclusive choices (`⊕`):** Model alternatives that cannot occur simultaneously—a natural fit for discriminated unions and sum types.
* **Hierarchy-driven flow (`Begin` → `Process` → `End`):** Every level in the output tree has a natural *before* (setup/header), *during* (per-item transformation), and *after* (summary/footer).

Drawn as a classic left-to-right bracket diagram, where each brace groups the parts that make up the set on its left, the processing structure for a store sales report looks roughly like this:

```text
                       ┌ Begin Report: print report header
                       │
                       │               ┌ Begin Store: print store header
                       │               │
                       │               │                     ┌ Sale (0,1)
Monthly Sales Report ──┤ Store (1,n) ──┤ Transaction (0,n) ──┤   ⊕
                       │               │                     └ Return (0,1)
                       │               │
                       │               └ End Store: print store net total
                       │
                       └ End Report: print company grand total
```

Every repeating level gets its own `Begin` and `End`, and `⊕` marks alternatives that exclude each other. Even if you never draw that bracket tree, thinking in that structure immediately clarifies your types and functions.

---

## A Six-Step Design Sequence (`LFUD → LOS → LIF → LPS → LOF → LRR`)

Step names and acronyms vary between textbooks and courses (DSSD program design, for example, calls the input side the *Logical Data Structure*), so treat the six labels below as this post's shorthand. Each step answers one question before you move on to the next:

| Step | Core Question It Answers | Practical Focus |
| --- | --- | --- |
| **LFUD** *(Problem & State Context)* | **What problem are we solving, and what state exists?** | Business goal, rules, master/transaction context, and constraints. |
| **LOS** *(Logical Output Structure)* | **What needs to come out, and in what hierarchy?** | Output tree, `(0, n)` / `(1, n)` repetition, and `⊕` choices—without formatting details. |
| **LIF** *(Logical Input File)* | **What inputs—and what grouping or ordering—are required?** | Records, keys, and grouping that line up with the `LOS`: the ideal input, not necessarily what upstream sends. |
| **LPS** *(Logical Processing Sequence)* | **How and in what order do inputs become outputs?** | `Begin` / `Process` / `End` stages, pure transformations, and aggregations. |
| **LOF** *(Logical Output Format)* | **How is the logical result rendered or serialized?** | Text layout, alignment, CSV/JSON serialization, or UI presentation. |
| **LRR** *(Logical Result & Refinement)* | **Does it hold up under edge cases and invalid data?** | Empty collections, negative/corrupt values, error handling, and type tightening. |

---

## Mini Walkthrough: Monthly Store Sales Summary in Python

The code requires Python 3.10+ (it uses `match` statements and `X | Y` union types).

### 0. Establish Context (`LFUD`)

We need a monthly sales summary covering one or more stores `(1, n)`. Each store may have zero or more transactions `(0, n)`. Every transaction is exclusively either a `SALE` (credited to net sales) or a `RETURN` (debited from net sales). Each store needs a net total, and the report needs a company-wide grand total.

### 1. Model the Output Structure First (`LOS`)

Starting from the output (`LOS`) lets us express `(1, n)`, `(0, n)`, and `⊕` directly as immutable types. Using `Decimal` avoids floating-point rounding drift in financial totals:

```python
from dataclasses import dataclass
from decimal import Decimal
from typing import Literal

ZERO = Decimal("0.00")


@dataclass(frozen=True)
class SaleLine:
    tx_id: str
    signed_amount: Decimal
    kind: Literal["SALE"] = "SALE"


@dataclass(frozen=True)
class ReturnLine:
    tx_id: str
    signed_amount: Decimal
    kind: Literal["RETURN"] = "RETURN"


TransactionLine = SaleLine | ReturnLine  # ⊕ alternation


@dataclass(frozen=True)
class StoreSection:
    store_id: str
    store_name: str
    lines: tuple[TransactionLine, ...]  # (0, n): stores with 0 transactions are valid
    net_total: Decimal


@dataclass(frozen=True)
class MonthlySalesReport:
    period: str
    stores: tuple[StoreSection, ...]  # (1, n): enforced at construction
    grand_total: Decimal

    def __post_init__(self) -> None:
        if not self.stores:
            raise ValueError("MonthlySalesReport requires at least one store (1, n).")
```

### 2. Align the Input Structure (`LIF`)

Once the `LOS` is fixed, the required input shape (`LIF`) becomes obvious: each `StoreSection` needs a `StoreMaster` paired with its `(0, n)` grouped transactions. Validating invariants in `__post_init__` means an invalid record can never be constructed:

```python
@dataclass(frozen=True)
class StoreMaster:
    store_id: str
    store_name: str


@dataclass(frozen=True)
class SaleRecord:
    tx_id: str
    store_id: str
    amount: Decimal
    tx_type: Literal["SALE"] = "SALE"

    def __post_init__(self) -> None:
        if self.amount <= ZERO:
            raise ValueError(f"Sale {self.tx_id} amount must be positive, got {self.amount}.")


@dataclass(frozen=True)
class ReturnRecord:
    tx_id: str
    store_id: str
    amount: Decimal
    tx_type: Literal["RETURN"] = "RETURN"

    def __post_init__(self) -> None:
        if self.amount <= ZERO:
            raise ValueError(f"Return {self.tx_id} amount must be positive, got {self.amount}.")


StoreTransaction = SaleRecord | ReturnRecord  # ⊕ alternation


@dataclass(frozen=True)
class StoreInputGroup:
    master: StoreMaster
    transactions: tuple[StoreTransaction, ...]  # (0, n)

    def __post_init__(self) -> None:
        for tx in self.transactions:
            if tx.store_id != self.master.store_id:
                raise ValueError(
                    f"Transaction {tx.tx_id} belongs to store {tx.store_id}, "
                    f"expected {self.master.store_id}."
                )
```

> **Why `LOS → LIF` helps:** If raw upstream data arrives as an ungrouped stream of transactions, designing `LIF` *after* `LOS` tells you immediately that a grouping/join step by `store_id` must happen before aggregation.

### 3. Express Processing Functionally (`LPS`)

Because `LIF` and `LOS` already enforce structural invariants at creation time, `LPS` stays free of defensive validation noise. Each level still follows `Begin` → `Process` → `End`: the `ZERO` start value is the *Begin*, mapping each child item is the *Process*, and the finished sum stored as `net_total` or `grand_total` is the *End*:

```python
def to_transaction_line(tx: StoreTransaction) -> TransactionLine:
    match tx:
        case SaleRecord(tx_id=tx_id, amount=amount):
            return SaleLine(tx_id=tx_id, signed_amount=amount)
        case ReturnRecord(tx_id=tx_id, amount=amount):
            return ReturnLine(tx_id=tx_id, signed_amount=-amount)


def build_store_section(group: StoreInputGroup) -> StoreSection:
    lines = tuple(to_transaction_line(tx) for tx in group.transactions)
    net_total = sum((line.signed_amount for line in lines), ZERO)
    return StoreSection(
        store_id=group.master.store_id,
        store_name=group.master.store_name,
        lines=lines,
        net_total=net_total,
    )


def build_report(groups: tuple[StoreInputGroup, ...], period: str) -> MonthlySalesReport:
    stores = tuple(build_store_section(group) for group in groups)
    grand_total = sum((store.net_total for store in stores), ZERO)
    return MonthlySalesReport(
        period=period,
        stores=stores,
        grand_total=grand_total,
    )
```

Because `to_transaction_line` declares its return type, a type checker such as mypy reports a missing return if you add a third `⊕` alternative (say, an `ExchangeRecord`) without handling it.

### 4. Separate Presentation (`LOF`) from Logic (`LPS`)

Keeping rendering in `LOF` means switching from plain text to JSON, CSV, or HTML requires zero changes to domain types or calculations. The renderer walks the same hierarchy as the `LOS`, and its comments match the bracket diagram above:

```python
def format_report(report: MonthlySalesReport) -> str:
    parts = [f"=== SALES REPORT ({report.period}) ==="]  # Begin Report
    for store in report.stores:  # Store (1, n)
        parts.append(f"\nStore: {store.store_id} - {store.store_name}")  # Begin Store
        if not store.lines:
            parts.append("  (no transactions)")
        for line in store.lines:  # Transaction (0, n)
            parts.append(f"  {line.tx_id}: {line.signed_amount:+.2f}")
        parts.append(f"  Store Net Total: {store.net_total:.2f}")  # End Store
    parts.append(f"\nCompany Grand Total: {report.grand_total:.2f}")  # End Report
    return "\n".join(parts)
```

### 5. Verify Edge Cases and Refine (`LRR`)

Finally, `LRR` asks what breaks at the boundaries—and enforcing invariants during object creation keeps those guarantees intact everywhere:

* **Empty stores tuple `(1, n)` violation:** `MonthlySalesReport.__post_init__` rejects `stores=()` loudly instead of silently outputting a `0.00` report.
* **Store with zero transactions `(0, n)`:** Valid—`build_store_section` computes `net_total = Decimal("0.00")`, and `format_report` prints `(no transactions)` clearly.
* **Corrupt input signs:** `SaleRecord.__post_init__` and `ReturnRecord.__post_init__` reject non-positive amounts on creation rather than masking upstream bugs with `abs(amount)`.
* **Mismatched `store_id` in inputs:** `StoreInputGroup.__post_init__` prevents cross-store transactions from leaking into the wrong store's net total.

---

## How This Connects to Hexagonal Architecture and DDD

If this separation feels familiar, it's because Warnier/Orr's data-driven discipline shares several ideas with **Hexagonal Architecture** and **Domain-Driven Design (DDD)**:

* **Hexagonal Architecture (Ports & Adapters):**
  * **Core (`LOS` + `LIF` + `LPS`):** Pure domain types and transformations with no I/O or presentation dependencies. `build_report` is the port the outside world calls.
  * **Inbound adapters (feeding `LIF`):** File readers, database queries, or API handlers that turn raw data into valid `StoreInputGroup` values. DSSD calls this step *physical input mapping*: converting the input you actually get into the logical input you designed.
  * **Outbound adapters (`LOF`):** Renderers like `format_report` (text, JSON, CSV, HTML) that serialize the `LOS` without leaking formatting concerns into `LPS`.
  * **One caveat:** The core never calls out, so there are no driven ports; `format_report` simply runs after `build_report` returns. Strictly speaking, that makes this example *Functional Core, Imperative Shell* rather than full Ports & Adapters.
* **Domain-Driven Design (DDD):**
  * **Always-valid domain objects:** Enforcing `amount > 0` in `SaleRecord.__post_init__` and `(1, n)` in `MonthlySalesReport.__post_init__` means an invalid object can never exist, so later steps can trust their inputs.
  * **Ubiquitous Language in types:** `SaleRecord | ReturnRecord` (`⊕`) and `(0, n)` vs. `(1, n)` put business rules into names and types a domain expert would recognize, instead of hiding them in scattered conditionals.
  * **Tactical, not strategic:** The overlap is with DDD's *tactical* patterns (entities, value objects, invariants). Warnier/Orr has nothing to say about *strategic* design (Bounded Contexts, Context Maps, Subdomains); it gives you a lightweight, **output-first** blueprint for a single use case or pipeline.

You don't need retro bracket diagrams on your wall to benefit from Warnier/Orr: working output-first from `LFUD` through `LRR` gives you cleaner boundaries, stronger invariants, and code that mirrors the problem domain.
