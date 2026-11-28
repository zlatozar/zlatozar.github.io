---
name: ddd-essence
description: Models one feature with Domain-Driven Design, in the words of its domain. It goes from the domain language to bounded contexts, value objects, entities, aggregates, domain services, life cycles as closed sets of states, guarded edges, and events that flow one way. Use before writing code for a feature that adds or changes business rules (who may do what, which state a thing is in, who must hear about a change), and when several parts of a system see the same thing in different ways. Also use to review a domain model, for example one with plain strings for domain values, setters on entities, an enum where the states carry data, or modules that depend on each other in a cycle. Not for bug fixes, renames, reports, plain data pipelines, or screens that only read and write rows without rules.
license: MIT. LICENSE.txt has the full text.
metadata:
  source: https://zlatozar.github.io/the-essence-of-domain-driven-design/
---

# The Essence of Domain-Driven Design

Model one feature in the words of its domain. Go through the phases in order. Each phase
answers one question, adds one part to the design note, and has one check. Where a phase
says **Stop**, show the note and wait for the user.

The method does not depend on a language. Each rule names the guarantee it needs, not a
language feature. Before you start, or when you resume at a later phase, read
[instrument-repair.md](references/instrument-repair.md): it shows a finished design note
for a small feature, and the code that follows it.

## When to Use

Use it when a feature adds or changes business rules: what is allowed, who decides, which
state a thing is in, and who must hear about a change.

Skip it when:

* the task is a bug fix, a rename, or a refactoring that does not change a rule;
* the feature only moves data: a report, an export, or a screen that reads and writes
  rows without rules. It needs no aggregates;
* nobody can say yet what the words mean. Talk with the domain experts first. An
  [EventStorming](https://zlatozar.github.io/event-storming/) workshop is one way.

If the project already has a glossary, a model, contracts, or modules, start from them.
Do not ask the user for what the project already says.

## What Comes From Other Layers

This skill decides the model of one feature. It does not override:

* **Domain:** the words come from the domain experts and the project's glossary. If a
  term is missing, ask. Do not invent domain words.
* **Architecture:** if the project already has modules, layers, or a message broker, use
  them. The defaults in [modules.md](references/modules.md) apply only where the project
  has no rule.
* **Platform:** who the caller is and how large a request may be are platform concerns.
  The skill says where these checks go, not how to build them.
* **Style:** follow the project's language, naming, error handling, and test conventions.

## Phase 1: What Do the Words Mean?

1. Write the feature as short sentences in the users' words, as the post starts from *"I
   send a medical instrument for repair."* Ask what each sentence means until every word
   in it has a definition.
2. Write a glossary: one line per term, and one meaning per term. This is the first law
   of logic: we cannot reason about a word that means two things. When two groups of
   people use one word in two ways, keep both meanings and note who uses each. This is a
   sign of two contexts (phase 2).
3. List the actions first: what is done, by whom, and who approves it. Then list only the
   properties that the actions need. A property that no action needs stays out of the
   model.
4. Write the facts as sentences that are true or false: *"A box contains one or more
   medical instruments."* Facts become relations in the database and invariants in the
   code.

If the sentences do not answer a question, list it as an open question. Do not guess.

**Check:** every noun and verb of the feature has one line in the glossary, and no term
has two meanings for one group of people.

**Stop.** Show the first part of the design note (see below). Wait for approval.

## Phase 2: Who Sees What?

Group the actions by the people who do them and the words they use. A group whose words
have a meaning of their own is a bounded context. The same thing can live in several
contexts, and each one sees only the part it needs: the mapping is not 1:1. In the
example, the department sees who owns an instrument, and accounting sees its price.

Write a table: each context, what it sees, and what it does not see. Then list the
events between the contexts: a name in the past tense, who sends it, who receives it, and
what it carries.

* Each fact is changed by one context. The others hear about it through events.
* Events flow one way. A cycle between two contexts means a wrong cut. Move the
  responsibility, or leave the way back out of the model and write it under **Left out**.

**Check:** every action belongs to exactly one context, and the events have no cycle.

**Stop** if the feature needs a new context or a new event between contexts: show the
note and wait for approval. Otherwise, show the note and go on.

## Phase 3: What Kind of Thing Is It?

Classify each concept of each context with the questions in
[building-blocks.md](references/building-blocks.md): value object, entity, aggregate,
domain service, factory, or domain event. Then check each aggregate against the post's
rules:

* the root is the only way in: no code outside the aggregate changes its entities;
* it holds no more than three kinds of entity;
* its ID is unique in the whole system. Use the natural ID if the domain has one;
* one command changes one aggregate, in one transaction;
* it is small: two users who change different things should not wait for each other.

**Check:** every command changes exactly one aggregate, and no domain value is a plain
string or number.

**Stop** if the design adds a new aggregate: show the note and wait for approval.

## Phase 4: What Must Always Be True?

Write each invariant as a sentence:

* for each value object: which values are valid;
* for each entity: what may change, and what never changes. Decide this carefully: each
  change you allow is one more rule to keep;
* for each aggregate: the rules across its entities, such as *"a box holds one to three
  instruments, all of one repair category."*

Give each invariant one owner and one check, as early as possible: the type, then the
constructor, then the method of the aggregate root, then a database constraint. Each
allowed change is a method on the root, named with a domain verb: `sendForRepair`, not
`setState`.

**Check:** every invariant has one owner and one check, and every change is a domain
verb on the root.

## Phase 5: How Does It Move Between States?

For each entity with a life cycle, draw the states and the moves as a text diagram:

```text
Received --start--> InRepair --finish--> Repaired(cost) or WrittenOff(reason)
```

* States without data are a closed list.
* States that carry data are a closed set of types, and each state holds only its own
  data. A repaired job has a cost; a written-off job has a reason. Then an illegal state
  cannot even be written: *make illegal states unrepresentable*.
* A move that is not on the diagram is refused as a conflict.
* Every command is safe to repeat: the same command a second time changes nothing. Then
  a client can fire and forget, and send the command again when an answer is lost.
* The errors are a closed set too, so the edge must give each one its answer.
* A finished thing leaves the view, not the data: *views only hide things*.

**Check:** every match over states or errors covers each case and has no default branch.
Each move that is not on the diagram is refused, and each command is safe to repeat.

## Phase 6: What Crosses the Edges?

Design the edges with [edges.md](references/edges.md):

* **Incoming:** the five checks, in order: origin, size, lexical content, syntax, and
  semantics. *Parse, don't validate:* the contract defines the input types, a mapper
  turns them into value objects, and after that no domain value is a plain string or
  number.
* **Outgoing:** be conservative in what you send. An event or a response carries only
  what its reader needs, in public types of its own, never the internal model.
* **Stored:** the database keeps the facts from phase 1 as relations, and each context
  has its own tables. A closed set of states with data gets the same rule in three
  places: the code, the contract, and the database.

**Check:** each of the five checks has one place, and no event or response shows an
internal type.

**Stop.** Show the whole design note. Wait for approval before you write code.

## Phase 7: Build It in Layers

Inside each context, build three layers. Dependencies point inward (see
[modules.md](references/modules.md)):

1. **Domain:** the value objects, entities, aggregates, domain services, factories, and
   events. Plain code: no framework, no database, no web.
2. **Application:** one use case per command, the ports it needs (a repository, an event
   publisher), and a service that runs each use case in one transaction. It loads the
   aggregate, calls the root, saves the aggregate, and then publishes its events. It only
   orchestrates; a business rule here belongs in the domain.
3. **Infrastructure:** the adapters for the web, the database, and messaging.

Other contexts see only the events. A listener runs after the sender's transaction is
committed, in a transaction of its own.

**Check:** guard tests check the module and layer rules in every build. If the language
has no tool for this, write the rules as tests over the imports.

## Phase 8: Verify

Write the tests from the design note:

* each value object: valid values, the first invalid value on each side, and its
  operations;
* each aggregate: each invariant, each move on the diagram, each move that is not on it,
  and each command sent twice;
* each of the five checks: one test at the edge, with its answer;
* every request and every response, checked against the contract;
* each event: the receiving context alone, and the same event delivered twice;
* one test that runs the whole feature across the contexts.

If a test fails, decide whether the code or the note is wrong. Do not change a rule that
the user approved without asking.

## Reviewing a Model

To review an existing model, run the check of each phase against the code. For each
broken rule, report the rule, the place in the code, and the phase that fixes it. Common
findings:

* domain values that stay plain strings or numbers after the edge;
* setters or public methods that change an entity, so the root is not the only way in;
* an enum with optional fields, where each state should carry its own data;
* a match with a default branch over a closed set;
* one command that changes two aggregates;
* contexts that depend on each other in a cycle, or events that carry the internal model;
* a long service method that micromanages each step. Verbose code means a wrong
  abstraction: look for the missing concept.

## Design Note

Show the design in this form before writing code. Phase 1 fills the first part, and
phases 2 to 6 add the rest. Keep it short.

```markdown
## Design: <feature>

**Sentences:** <the feature in the users' words>
**Glossary:** <term: one meaning; per context where the meanings differ>
**Actions:** <what is done, by whom, and who approves it>
**Facts:** <sentences that are true or false>
**Open questions:** <what the sentences do not say>

**Contexts:** <table: context | sees | does not see>
**Events:** <name in the past tense: from -> to, and what it carries>
**Building blocks:** <table: concept | kind | ID or values | context>
**Invariants:** <rule -> owner -> check>
**Life cycles:** <a state diagram per entity, and the data of each state>
**Edges:** <where each of the five checks lives; what each event and response carries>
**Left out:** <what the model does not show, on purpose, and why>
```

## Rules

* Name types and methods in domain words: `sendForRepair`, not `updateStatus`.
* Ask instead of guessing. Do not invent domain words or rules.
* Put each rule at its level of abstraction: a value object, an entity, an aggregate, or
  a bounded context.
* Write down what the model leaves out, and why. A model is a choice.
