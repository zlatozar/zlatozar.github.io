# The Edges

Data crosses an edge three times: when a request comes in, when an event or a response
goes out, and when an aggregate is stored. Each edge has its own rules.

## Incoming: Five Checks in Order

Refuse bad data as early as possible, with the cheapest check that can refuse it. The
order comes from *Secure by Design*.

| # | Check | The question | Typical place | Answer |
| --- | --- | --- | --- | --- |
| 1 | Origin | Does the request come from someone we know and allow? | the platform: authentication | 401 / 403 |
| 2 | Size | Is it small enough to read? | a filter, before the body is read | 413 / 400 |
| 3 | Lexical content | Are the characters and the lengths right? | the contract: patterns, lengths, lists of allowed values | 400 |
| 4 | Syntax | Is the structure right: required fields and their types? | the types generated from the contract | 400 |
| 5 | Semantics | Does it make sense in the domain? | value objects, then the aggregate | 422 / 404 / 409 |

The syntax answer lists every broken field at once, so the client can fix them all in
one go. The domain sees only data that has passed the first four checks.

## Parse, Don't Validate

```text
request -> contract types (checks 3 and 4) -> mapper -> value objects (check 5) -> use case
```

Write no parser and no field-by-field validation by hand. The contract defines the input
types, and a generator makes them. If the data cannot become these types, it is invalid.
A mapper then turns them into value objects. After that, no domain value is a plain
string or number.

Some rules appear twice on purpose, such as the form of an instrument ID. The contract
tells the client early and clearly what is wrong. The value object is the truth inside
the application, and it holds when the data comes from somewhere else: an event, a file,
or the database.

The two places catch different mistakes. A damage note of only spaces passes the
contract, because it is a string of the right length. The `Damage` value object refuses
it with 422, because it says nothing.

## Outgoing: Conservative in What You Emit

The post's rule: *"Be liberal with what you accept, and conservative in what you emit."*
Once a field is in a contract, somebody depends on it. It is always easier to add a field
than to remove one.

* An event carries only what its receiver needs. `RepairBoxApproved` carries the
  instruments and their damage, and no prices.
* An event uses public types of its own, never the sender's internal model. Then the
  sender can change its model without breaking the receiver.
* A receiver reads only what concerns it. Accounting reads the cost of a repair and
  ignores the reason of a write-off.
* A response wraps a list in an object, `{ "jobs": [...] }`, not a bare list, so the
  response can grow without breaking clients.
* Closed alternatives become a `oneOf` with a field that names the case, such as
  `result`. The case decides which other fields are required.

## Stored: What the Database Contains

* The database keeps the relations, and the domain interprets them. *"A box contains one
  or more medical instruments"* becomes a table that links an instrument to its box.
* Each context has its own tables and its own repository. The same instrument appears in
  three contexts, each with only its own columns.
* Store facts, not what can be calculated. Accounting stores each repair, and the asset
  adds up their cost.
* The domain knows nothing about the database. A repository adapter maps the aggregate
  to rows by hand and saves the whole aggregate at once.
* A command reads its aggregate with a lock (`SELECT ... FOR UPDATE`), so two changes at
  the same time wait for each other.

## The Same Rule in Three Places

A closed set of states with data keeps its rule in the code, the contract, and the
database. For *"a repaired job has a cost"*:

| Place | How the example keeps the rule |
| --- | --- |
| Code | `Repaired(Money cost)`: a repaired job cannot be built without a cost |
| Contract | the outcome is a `oneOf`, and `result: REPAIRED` requires `cost` |
| Database | `CHECK ((state = 'REPAIRED') = (cost_amount IS NOT NULL))` |

Each place stops a different writer: the code stops a programmer, the contract stops a
client, and the database stops a script that changes rows by hand.
