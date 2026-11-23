---
name: warnier-orr-design
description: Designs one use case from its output back to its input, with the Warnier/Orr method. Use before writing code for a task that produces or transforms structured data, such as a report, export, file format, parser, API response, message, or batch job, and when input and output have different structures (pages, batches, chunks, paged APIs, mixed event streams). Not for bug fixes, renames, or small edits.
license: MIT. LICENSE.txt has the full text.
metadata:
  source: https://zlatozar.github.io/warnier-orr-diagrams/
---

# Warnier/Orr Design

Design one use case starting from what comes out, then work back to what goes in. Go
through the phases in order. Each phase answers one question, produces one artifact, and
has one check. Where a phase says **Stop**, show the artifact and wait for the user.

Before you start, or when you resume at a later phase, read
[sales-report.md](references/sales-report.md): it shows a finished design note for a
small ticket.

## When to Use

Use it when a task produces or transforms data whose structure you can write down:
reports, exports, file formats, parsers, API responses, messages, and batch jobs.

Skip it when:

* the task is a bug fix, a rename, or a refactoring that does not change any output;
* the hard part is the timing of events (GUI, workflow, device control), an algorithm, or
  concurrency;
* nobody knows yet what should come out. Suggest a prototype first.

If a schema, an API contract, or existing tests already fix the output, use them as the
sample in phase 1. Do not ask the user for a new one.

## What Comes From Other Layers

This skill decides the structure *inside* one use case. It does not override:

* **Domain:** take names from the project's domain language: glossary, existing types,
  documentation. If a term is missing, ask. Do not invent domain words.
* **Architecture:** the project decides where I/O lives. By default, parsing and
  formatting stay at the edges, and the core is pure.
* **Style:** follow the project's language, naming, error handling, and test conventions.

## Phase 1: What Comes Out?

Answer five questions from the ticket:

1. **What comes out?** Write a sample of the output by hand, with real values. Write the
   bad case too: what does the user see when the input data is wrong? A broken setup,
   such as a missing file, is not part of the bad case; it fails fast.
2. **How is it nested?** For each item in the sample, ask how often it appears. The
   answers give the levels: an item printed once per store belongs to the store level.
3. **How many?** For each level: optional `(0,1)`, zero or more `(0,n)`, or at least one
   `(1,n)`. Each answer is a rule, and later a check.
4. **Which excludes which?** Parts that are one or the other, never both, get `⊕`.
5. **Logical or physical?** Keep what the output says apart from how it looks: layout,
   column widths, field names, encoding. The physical part belongs to the edge. If the
   layout has groups of its own, such as pages, batches, or chunks, write them as a
   second tree, the *physical tree*.

Write the output structure as an indented tree. A line holds a level and its count, and
`⊕` stands between alternatives:

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

If the ticket does not answer a question, for example how the program is started or what
happens with bad rows, list it as an open question. Do not guess. If a sample needs an
answer anyway, propose one, mark it as a proposal, and link it to the question. Shorten a
long sample with a line such as `… 46 more lines …`; the golden test in phase 6 needs
the full text.

**Stop.** Show the first part of the design note (see below). Wait for approval.

## Phase 2: What Goes In?

Take the output tree and remove everything that can be calculated, plus fixed text such
as titles. What remains is the required input, with the same nesting. Keep two kinds of
item that are not printed: keys that the grouping or the order needs, such as a customer
id, and inputs that only a rule needs, such as the period for a date check.

Then compare it with the real input: a sample file, an API response, or a schema. If you
have only a list of columns, ask for a few real lines.

**Check:** every item of the output is either calculated or comes from the input.

Then compare the trees in pairs: each real input against the output tree, and the output
tree against the physical tree, if there is one. If a pair has different groups or a
different order, run the clash check in [clashes.md](references/clashes.md). For each
clash, ask its cost questions from there: for an ordering clash, how large the input can
grow; for an interleaving clash, how a group ends and what happens to groups still open
when the input stops. When the steps run together, as in a boundary or an interleaving
clash, also ask whether the user may see part of the output when a later item is bad.

**Stop** if there is a clash or a new open question: show the whole design note, and
wait for approval. Otherwise, show the note and go on.

## Phase 3: Build the Core

Turn each level into a type. Use [marks.md](references/marks.md) to choose the language
construct for each mark. Write one pure function per level: what happens before the
children (Begin), each child (Process), and what happens after them (End). Totals are a
fold; each `⊕` is an exhaustive match. No I/O in the core.

**Check:** the type checker passes, and every `⊕` has an exhaustive match.

## Phase 4: Parse the Input

Write the step from raw input to the input types, at the edge. Expected bad data, such as
a broken row, is collected and returned as the bad-case output from phase 1. A broken
setup or a broken invariant in the core is a bug: fail fast.

If phase 2 found a clash between the real input and the output tree, solve it here, as
designed there. For an ordering clash, group the rows by key and sort them; if the input
is too large to read whole, sort it before the parser, with an external sort or with
`ORDER BY` in the database. For an interleaving clash, keep one state per group key and
apply the end rules from phase 2.

**Check:** every rule from phase 1 has a place where it is checked. Put each rule as
early as the language allows: type, then constructor, then parser, then test.

## Phase 5: Format the Output

Write the step from the core result to the physical output, at the edge. Only this step
knows the layout. If phase 2 found a clash between the output tree and the physical
tree, as with stores and pages, build this step as the two programs and the lazy stream
designed there.

## Phase 6: Verify

Turn the phase 1 samples into golden tests: feed in the input that should produce each
sample, and compare the whole output. Then add tests for the edges that the marks create:
an empty `(0,n)`, a single `(1,n)`, and each side of every `⊕`.

If steps run together, add a laziness test: feed the steps between the edges an input
that fails when read past N items, with N larger than the first result needs, and take
only the first result. It fails if any step builds a list.

If a golden test fails on the first run, decide whether the code or the sample is wrong.
Do not change a sample that the user approved without asking.

## Design Note

Show the design in this form before writing code. Phase 1 fills the first part, and
phase 2 adds the rest. Keep it short.

```markdown
## Design: <use case>

**Output sample:** <good case; mark proposed values>
**Bad case:** <what the user sees when the input data is wrong>
**Output tree:** <indented tree>
**Physical tree:** <only if the layout has groups of its own>
**Open questions:** <what the ticket does not say>

**Input tree:** <indented tree, with the real input next to it>
**Calculated:** <items the program computes>
**Clash check:** <for each pair of trees: no clash, or the kind of clash, what the stream
carries, and the answers to its cost questions>
**New questions:** <what the comparison with the real input raised>
```

## Rules

* Do not draw box diagrams unless the user asks. Indented trees are enough.
* Ask instead of guessing.
* Join steps that run together with a lazy stream, never with a list. Read the whole
  input or build the whole output only where the design note says why.
* Name types and functions in domain words, not in method words. Where the domain has
  no word, as for the bad-case output, use a plain word and list it, so the user can
  change it.
