# Building Blocks and Their Guarantees

Choose a construct by the guarantee it gives, not by its name. The method needs only
these guarantees, so any language fits. Where a language gives a weaker guarantee, the
check moves one step later: the type, then the constructor, then the aggregate root, then
the database, then a test.

## Which Kind Is It?

For each concept, ask these questions in order and stop at the first yes:

1. Does it have an identity that lives longer than its attributes, and do we follow its
   life? It is an **entity**. A scalpel is still the same scalpel after it is sharpened.
2. Is it defined only by its values? It is a **value object**. Two amounts of 45.50 EUR
   are the same money.
3. Is it a decision that belongs to none of the objects? It is a **domain service**.
   Whether to repair or replace an instrument depends on its owner and its damage, and
   neither of them should decide alone.
4. Is it something that happened, which another context must hear about? It is a
   **domain event**, named in the past tense: `RepairBoxApproved`.

Then group the entities:

5. Which entities must change together to keep one rule? They form one **aggregate**,
   and one of them is its root.
6. Does a new aggregate start by rules of its own? Give it a **factory**.

## The Guarantees

| Building block | The construct must | Checked by | In the example (Java) |
| --- | --- | --- | --- |
| Value object | have fields that cannot change, be equal when its values are equal, and refuse an invalid value when it is built | type, constructor | `record Money` |
| Entity | be equal when its ID is equal, and change only through methods that its aggregate root can call | type, visibility | `Instrument`: package-private methods |
| Aggregate root | be the only way to change its entities, and collect the events it raises | visibility, architecture test | `Department` |
| Domain service | hold no state, and answer with a closed set of decisions | type | `Triage.classify` returns a sealed `Decision` |
| Factory | be the only way to start a new aggregate; the constructor only rebuilds saved data | review | `RepairJobFactory.jobsFor` |
| Domain event | be a fact that cannot change, and use public types of its own | type, architecture test | `RepairBoxApproved` |
| Closed set of states | close the set of alternatives, and fail the build when a match misses one | type | sealed `JobState` |
| Closed set of errors | the same as the states, so the edge must map each error to an answer | type | sealed `DepartmentException` |

"Visibility" means that the language can hide a method from code outside the aggregate.
Where it cannot, use a naming convention and check it with an architecture test.

## Value Objects

* A value object cannot change. An operation returns a new value: `Money.add` returns
  new money.
* It checks itself when it is built, so an invalid one cannot exist. `Money` refuses a
  negative amount, more decimals than the currency has (cents in yen), and pseudo
  currencies such as gold.
* It has an algebra: the operations the domain needs, and only those. Money can be added
  and compared. It is never multiplied by money, and amounts in two currencies are never
  added.
* A named factory turns text into a value: `Money.of("45.50", "EUR")`.
* It has no ID.

## Entities

* Use a natural ID: the code engraved on the instrument, or the number written on the
  box. Use a generated ID only when the domain has none.
* Two entities are equal when their IDs are equal, whatever their other fields say.
* An entity lives long and is saved in the database.
* Decide carefully what may change. Each allowed change is a method with a domain verb,
  and only the aggregate root can call it.

## Aggregates

The post's rules for writing aggregates, and how to keep them:

| Rule | How |
| --- | --- |
| The entry point for every operation | Only the root has methods that change state. Adapters call use cases, never the domain. |
| No more than three kinds of entity | Count them in the design note. More means two aggregates. |
| A global ID | A natural ID, or one built from the IDs of its parents: `SURGERY-1-SC-1042`. |
| All objects valid, invariants after every change | Value objects check themselves. The root checks the rules across its entities before it changes anything. |
| Saved in the database | One repository per aggregate. Save the whole aggregate in one transaction. |

Keep an aggregate small. In the example, the workshop has one `RepairJob` per instrument,
not one per box. A technician who finishes one job does not lock the other jobs of the
same box.

A command names the action, not its steps. The caller writes
`department.sendForRepair(instrument, damage)`. The root marks the instrument, asks the
domain service, and puts the instrument in a box or requests a new one. The caller does
not micromanage these steps.

## Domain Services

A domain service makes one decision, holds no state, and answers with a closed set of
decisions. The post's convention is `Class.staticMethod()`. The aggregate root calls it
and acts on the answer:

```java
public static Decision classify(Ownership ownership, Damage damage) {
  return switch (ownership) {
    // We do not repair what we do not own; the owner sends a new one
    case ThirdParty(Supplier owner) -> new Replace(owner);
    case Ours() ->
        new Repair(
            switch (damage.kind()) {
              case BLUNT -> RepairCategory.SHARPENING;
              case BENT, BROKEN -> RepairCategory.MECHANICAL;
            });
  };
}
```

## Factories

A new aggregate often starts by rules: a repair job starts from an approved box, one job
per instrument, in the state `Received`. Put these rules in a factory
(`RepairJobFactory.jobsFor(box)`). The constructor only rebuilds an aggregate from saved
data, so it decides nothing.

## An Exhaustive Match

Adding a state must break the build at every match that misses it. A default branch that
swallows the rest breaks this rule: the new state passes in silence.

```java
public void start() {
  switch (state) {
    case Received _ -> state = new InRepair();
    case InRepair _ -> {
      // Already started
    }
    case Outcome _ -> throw new AlreadyFinished(id);
  }
}
```

The same match shows two more rules. A second `start` changes nothing, so the command is
safe to repeat. A move that is not on the diagram is refused.

Most typed languages check a match like this without help, as long as there is no default
branch. Where one does not, the default branch must fail the type check, as
`assert_never` does in Python.
