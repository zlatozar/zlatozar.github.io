# Worked Example: Instrument Repair

This is the example from the article [The Essence of Domain-Driven Design][post], written
as the design note the skill produces before any code. The [companion project][project]
is the code: a Spring Boot application in Java.

## The Request

> Here is an invented action from our process: "I send a medical instrument for repair."
>
> 1. We mark the instrument as unusable.
> 2. We classify the instrument.
> 3. We put the instrument in the box for broken instruments.
> 4. Or we request a new one.

And the facts, from the same article:

> * A box contains one or more medical instruments.
> * Instruments are either ours or third-party.
> * A repair is approved by the director of the hospital department.

The feature follows the instrument through every context it touches: from the nurse who
sends it, to the cost in accounting.

## Design: Instrument Repair

**Sentences:**

* A nurse sends a damaged instrument for repair.
* The department puts it in a repair box, or asks its owner for a new one.
* The director approves the box, and its instruments go to the workshop.
* A technician repairs each instrument at a cost, or writes it off for a reason.
* A nurse records the return of a repaired instrument.
* Accounting adds the cost of each repair to the instrument's asset.

**Glossary:**

| Term | Meaning |
| --- | --- |
| Instrument | a medical instrument with a code engraved on it, such as `SC-1042` |
| Owner | who the instrument belongs to: the hospital, or a third-party supplier that leases it |
| Damage | what the nurse saw: blunt, bent, or broken, and a short note for the technician |
| Repair category | the kind of work: sharpening for a blunt instrument, mechanical for a bent or broken one |
| Repair box | one to three instruments of one repair category, sent to the workshop together |
| Approval | the director's decision to send a box to the workshop |
| Repair job | the workshop's work on one instrument from an approved box |
| Outcome | how a job ends: repaired at a cost, or written off for a reason |
| Asset | an instrument as accounting sees it: its purchase price and its repairs |
| Uneconomical | an asset whose repairs have cost more than the instrument: a hint for the next purchase, not a rule |

"Instrument" means something different in each context: a thing on the shelf with an
owner and a state in the department, a repair job in the workshop, and an asset in
accounting.

**Actions:**

* A nurse sends an instrument for repair. Nobody approves this.
* The director approves a repair box.
* A technician starts a job and records its outcome.
* A nurse records the return of a repaired instrument.
* Accounting records each repair and each write-off. Nobody does this by hand: it
  follows the workshop's events.
* An accountant looks at an asset.

**Facts:**

* A box contains one or more medical instruments, and at most three.
* The instruments in one box need the same repair category.
* Instruments are either ours or third-party.
* A repair is approved by the director of the hospital department.
* A repair job belongs to one instrument from one approved box.
* A repaired job has a cost. A written-off job has a reason.

**Open questions:** answered before the code. How many instruments fit in a box? Three.
Who tells the supplier that a new instrument is needed? Nobody in this model (see
**Left out**).

**Contexts:**

| Context | Sees | Does not see |
| --- | --- | --- |
| department | instruments, their owner and state, repair boxes | any price |
| workshop | the job, the damage, the outcome and its cost | the purchase price |
| accounting | the purchase price and the repairs | boxes, damage, leased instruments |

The article's rule: *"the worker who repairs the medical instrument does not need to know
how much money was paid for the instrument."*

**Events:**

* `RepairBoxApproved`: department -> workshop. The box, its repair category, and each
  instrument with its damage. No prices.
* `InstrumentRepaired`: workshop -> accounting. The job, the instrument, and the cost.
* `InstrumentWrittenOff`: workshop -> accounting. The job, the instrument, and the
  reason. Accounting ignores the reason.
* `ReplacementRequested`: department -> nobody yet. The instrument, its supplier, and the
  damage.

**Building blocks:**

| Concept | Kind | ID or values | Context |
| --- | --- | --- | --- |
| Money | value object | an amount and a currency | shared kernel |
| InstrumentId | value object | the engraved code: `SC-1042` | shared kernel |
| Damage | value object | a kind and a note | department; the workshop has its own copy |
| Ownership | value object | ours, or third-party with its supplier | department |
| Department | aggregate root | `SURGERY` | department |
| Instrument | entity | `SC-1042` | department |
| RepairBox | entity | a number in its department: box 1 of `SURGERY` | department |
| Triage | domain service | repair in a category, or replace | department |
| RepairJob | aggregate root | `SURGERY-1-SC-1042`: department, box, instrument | workshop |
| RepairJobFactory | factory | one job per instrument of an approved box | workshop |
| Asset | aggregate root | `SC-1042` | accounting |

**Invariants:**

| Rule | Owner | Check |
| --- | --- | --- |
| Money is not negative, has no more decimals than its currency, and is not a pseudo currency such as gold | `Money` | constructor |
| An instrument ID has the engraved form: two to four capital letters, a hyphen, and up to eight digits | `InstrumentId` | constructor; contract `pattern` |
| A damage note has 1 to 200 characters and is not only spaces | `Damage` | constructor; contract `maxLength` |
| A box holds 1 to 3 instruments, all of one repair category | `RepairBox` | constructor; `Department` opens a new box when one is full |
| We do not repair what we do not own: the owner sends a new one | `Triage` | an exhaustive match over `Ownership` |
| Only a usable instrument is sent for repair; sending it again changes nothing | `Department` | `sendForRepair` |
| Only an instrument at the workshop can return | `Department` | `returnFromRepair`: 409 |
| A repaired job has a cost; a written-off job has a reason of 1 to 200 characters | `JobState` | types; contract `oneOf`; database `CHECK` |
| A job finishes only after it starts, and keeps its outcome | `RepairJob` | `finish`: 409 |
| The same repair is counted once | `Asset` | `recordRepair` |

**Life cycles:**

```text
Instrument (department):
  USABLE --send for repair--> WAITING_FOR_REPAIR --box approved--> AT_WORKSHOP --return--> USABLE
     \--send for repair, third-party--> WAITING_FOR_REPLACEMENT (end)

Repair box (department):
  OPEN --approve--> APPROVED (end)

Repair job (workshop):
  Received --start--> InRepair --finish--> Repaired(cost) or WrittenOff(reason)

Asset (accounting):
  ACTIVE --written off--> WRITTEN_OFF (still takes the cost of a repair that was done)
```

The department's view shows only open boxes. An approved box leaves the view, not the
data.

**Edges:**

* Incoming:
  1. Origin: not built (see **Left out**). It belongs to the platform, in `apiguard`.
  2. Size: `apiguard`, one property for the largest request.
  3. Lexical content: the contracts in `api/*.yaml`: patterns, lengths, and lists of
     allowed values.
  4. Syntax: the classes generated from the contracts. The answer lists every broken
     field.
  5. Semantics: the value objects and the aggregates. 422 for an invalid value, 404 for
     an unknown instrument or job, 409 for a move that is not on the diagram.
* Outgoing: the events above, in public types of their own. The open jobs come wrapped
  as `{ "jobs": [...] }`. The outcome is a `oneOf`, and its `result` field names the
  case.
* Stored: each context has its own tables.
  * `repair_box_item` links an instrument to its box.
  * An empty `instrument.supplier` means that the instrument is ours.
  * `asset_repair` keeps each repair, and the asset adds up their cost.
  * `repair_job` keeps the outcome rule with `CHECK` constraints.

**Left out:**

* Write-offs in the department. The department never hears about a write-off, so a
  written-off instrument stays `AT_WORKSHOP`. A department listener would make a cycle.
  In real life, a nurse would record it, like a return.
* The supplier. `ReplacementRequested` is published, but nobody listens.
* Origin. There is no authentication, so anyone can approve a box.
* Exchange rates. Money in different currencies is never added.
* Master data. The department `SURGERY`, the scalpel `SC-1042`, the forceps `FO-2001`,
  and the endoscope `EN-7` are loaded at startup. The endoscope is leased from MedLease,
  so accounting does not know it and answers 404.

## Rules and Their Checks

| Rule | Check |
| --- | --- |
| Only the root changes its entities | package-private methods; `webCallsUseCasesOnly` |
| No domain value is a plain string or number | value objects; a mapper at each edge |
| A match over states or errors covers each case | sealed types, no default branch |
| Every command is safe to repeat | `sendingTwiceChangesNothing`, `theSameOutcomeTwiceChangesNothing` |
| A move that is not on the diagram is refused | `onlyAnInstrumentAtTheWorkshopCanReturn`, `aJobFinishesOnlyAfterItStarts`, `aFinishedJobKeepsItsOutcome` |
| Events flow one way | allowed dependencies per module; `modulesKeepTheirBoundaries` |
| Public events hide the internal model | `publicEventsHidePrivateTypes` |
| Listeners run after commit | `modulesListenAfterCommit` |
| Every request and response matches the contract | `ContractCheck` in the API tests |

## The Phases in Code

1. **Words:** the glossary gave the type names: `Department`, `RepairBox`, `Damage`,
   `Triage`, `RepairJob`, `Asset`. The use cases are named after the actions:
   `sendForRepair`, `approveRepairBox`, `returnFromRepair`, `startRepair`,
   `recordOutcome`.
2. **Contexts:** three Spring Modulith modules (`department`, `workshop`, `accounting`)
   and `sharedkernel`. Each module has its public events in its `events` package.
3. **Building blocks:** jMolecules annotations (`@ValueObject`, `@Entity`,
   `@AggregateRoot`, `@Factory`, `@DomainEvent`) name the kinds in the code.
4. **Invariants:** the constructors of the value objects, the methods of the roots, and
   the `CHECK` constraints in `schema.sql`.
5. **States:** `InstrumentState` is an enum, because its states carry no data. `JobState`
   is a sealed interface, and each of its records carries only its own data. The errors
   are the sealed classes `DepartmentException` and `WorkshopException`.
6. **Edges:** the contracts in `api/`, the classes generated from them, and one mapper
   per controller. `RequestSizeLimit` in `apiguard`. Event mappers in `messaging`, and a
   repository adapter in `persistence`.
7. **Layers:** `internal/domain`, `internal/application` (`port/in`, `port/out`,
   `service`), and `internal/infrastructure` (`web`, `persistence`, `messaging`), checked
   by the guard tests.
8. **Verify:** `DepartmentTest`, `RepairJobTest`, `AssetTest`, and `MoneyTest` for the
   domain. `*ModuleIT` for each context alone. `*ApiIT` through `ContractCheck` for the
   edges. `RepairFlowIT` for the whole feature.

[post]: https://zlatozar.github.io/the-essence-of-domain-driven-design/
[project]: https://github.com/zlatozar/zlatozar.github.io/tree/main/examples/the-essence-of-domain-driven-design
