# PrintersDevil (Simplified)

Companion project for [Why Warnier/Orr Thinking Still Matters][post].

This is a small text formatter. It is a simplified version of *PrintersDevil* from Charles
Wetherell's [*Etudes for Programmers*][etudes] (1978). The article shows the six
Warnier/Orr passes on a small sales report. This project takes the same six passes to a
bigger problem, and meets one new idea on the way: the **structure clash**.

How to read the code:

* Every module docstring shows the diagram that its code follows. Read the diagram and
  the code side by side.
* Comments like `# Page (0,n)` or `# ⊕` mark the bracket of the diagram that a line of
  code belongs to.
* Comments that start with `Modern Python:` mark a Python feature or a newer idea that
  the classic method did not have.

[post]: https://zlatozar.github.io/warnier-orr-diagrams/
[etudes]: https://www.amazon.com/Etudes-Programmers-Charles-Wetherell/dp/0132918072
[skills]: https://agentskills.io/

## Run It

It needs Python 3.12 or newer and [uv](https://docs.astral.sh/uv/).

```bash
just sync         # create .venv with uv
just demo         # format resources/example-page.txt as text
just demo-json    # the same pages as JSON
just demo-error   # a broken file: the error names the line
just check        # Ruff lint and format checks, then the ty type checker
```

Or directly: `uv run printers-devil [--format text|json] FILE`.

The input mixes text with commands. A command line starts with `?`:

```text
?papersize 10 56
?margin 3 54
?center
PRINTER'S DEVIL SAMPLE
?blank 1
?paragraph 3 0
Warnier and Orr showed that starting from the output
hierarchy makes tricky formatting rules much easier to
...
```

## The Big Picture

The program is one pipeline. The core in the middle is pure Python: no files, no
printing and no `?commands`. Adapters at the edges talk to the outside world.

```text
              inbound adapter      core: domain/ (pure, no I/O)         outbound adapter
             ┌───────────────┐   ┌──────────────────────────────────┐   ┌───────────────┐
markup file ─┤ markup.py     ├─► │ typeset() ─► lines ─► paginate() ├─► │ plain_text.py ├─► stdout
             │ text -> blocks│   │ blocks -> lines -> pages         │   │ json_pages.py │
             └───────────────┘   └──────────────────────────────────┘   └───────────────┘
                     Iterable[Block]                          Iterable[Page]
                     (the LIF)                                (the LOS)
```

`cli.py` is the composition root: the only module that knows all the parts. Every stage
is a generator, so the stages run together, one item at a time.

DDD and Hexagonal Architecture, as in the article:

* **Ubiquitous Language:** the types use the words of typesetting: `Page`, `Paragraph`,
  `Layout` (margins, indent, gap), `PageBreak`. No methodology words in the names.
* **Value Objects:** every domain type is a frozen dataclass that checks its own rules in
  `__post_init__`. An invalid block cannot exist, so the core needs no error checks.
* **Domain services:** `typeset` and `paginate` are pure functions.
* **Anti-corruption layer:** `markup.py` keeps the `?command` syntax out of the core.
* There are no Entities, Aggregates or Repositories. This problem has no identity and no
  storage, so it does not need them. Do not add patterns that the problem does not ask
  for.
* One caveat: the core never calls out, so there are no driven ports. Strictly speaking
  this is *Functional Core, Imperative Shell*.

## The Problem and Its State

The user writes plain text with commands in it. Commands change the **settings**. Text
is printed with the settings in force at that moment.

| Command | Meaning | Default |
| --- | --- | --- |
| `?papersize H W` | page height `H` lines, paper width `W` columns; resets the margins to `1 W`; starts a new page | `40 72` |
| `?margin L R` | left and right margin, in columns from 1; `R` must be inside the paper | `1 72` |
| `?mode M` | `fill`, `justify` or `unfilled` (see below) | `fill` |
| `?paragraph [I [G]]` | first-line indent `I` (negative: a hanging line), `G` empty lines before each paragraph | `3 0` |
| `?center` | print the next text line in the middle between the margins | |
| `?blank [N]` | print `N` empty lines | `1` |
| `?page` | start a new page | |

The modes:

* `fill`: put as many words on a line as fit.
* `justify`: like `fill`, then add spaces between the words, so the line ends exactly at
  the right margin. The last line of a paragraph is never stretched.
* `unfilled`: print the lines as typed until the next command. Good for tables.

The rules:

1. An empty line or any command ends a paragraph. The paragraph keeps the old settings.
2. One space between words, also after a sentence.
3. A word must fit on a line. Words are never broken.
4. Gap lines before a paragraph are not printed at the top of a page. `?blank` lines are.
5. Many `?page` commands in a row start one new page. An empty document has no pages.
6. The page heading and footing do not count in the page height.

**The state.** The settings are the only state in this problem, and they change while
the file is read. Our first design decision: the state stays at the edge, in the inbound
adapter. Every block takes a copy of the layout it needs, so the core has no state at
all. This makes rule 1 free: a finished paragraph already has its own copy of the old
settings.

## Pass 1 - Define the Result (LOS)

Forget the commands and look only at the demo output. Use LFUD. **List** the atoms: the
page number and the text of each line. Give the **frequency** of each, "once per what?":
the page number once per page, the text once per line. The "what"s are the
**universals**, and each becomes a bracket: a document has zero or more pages, a page
has one to `h` lines. Then **draw**:

```text
Document (1) {
  Page (0,n) {         (0,n): an empty document gives no pages
    Line (1,h)         (1,h): a page has 1 to h lines; h is the page height
  }
}
```

The heading `--- Page 1 ---` is not in the drawing. It says how a page *looks*, not what
a page *is*: it is physical, so it belongs to the formatter (Pass 5). In code the LOS is
one Value Object:

```python
Page(number=1, lines=("   Warnier and Orr", "showed that output"))
```

`Page.__post_init__` rejects a page without lines, so `(1,h)` is a rule that the code
cannot break.

## Pass 2 - Derive the Input (LIF)

Now ask: which input would make this output easy to build? Not the raw text with
`?commands` in it. The ideal input is a list of blocks, and *every block already knows
its own layout*:

```text
Document (1) {
  Block (0,n) {
      Paragraph  (0,1) { Word (1,n) }       filled, or justified
    ⊕ Unfilled   (0,1) { Line (1,n) }       printed as typed
    ⊕ Centered   (0,1)                      one centered line
    ⊕ BlankLines (0,1)                      empty lines from "?blank"
    ⊕ PageBreak  (0,1)                      start a new page
  }
}
```

The markup on the left becomes the block on the right:

```text
?margin 3 54                    Paragraph(
Warnier and Orr showed    ->        layout=Layout(left=3, right=54, indent=3, gap=0),
that output comes first.            words=("Warnier", "and", "Orr", ..., "first."),
                                    justify=False,
                                )
```

In Python the `⊕` is a union type, named with the `type` statement:
`type Block = Paragraph | Unfilled | Centered | BlankLines | PageBreak`. The inbound
adapter `markup.py` turns the real input into this ideal input (Pass 4).

## Pass 3 - Design the Process (LPS)

### The structure clash

The same lines belong to two trees at the same time:

```text
Document {                       Document {
  Block (0,n) {                    Page (0,n) {
    Line (1,n)                       Line (1,h)
  }                                }
}                                }
the input view (LIF)             the output view (LOS)
```

In the sales report of the article, one tree fits inside the other: a store never stops
in the middle of a transaction. Here this is not true. A paragraph can start on one page
and end on the next one. Here is the demo (`resources/example-page.txt`, page height 10),
line by line:

```text
typeset: block tree    line stream                        paginate: page tree

PageBreak h=10    ─    PageBreak h=10                ─    sets h=10, adds no line
Centered          ─    TextLine "PRINTER'S DEVIL…"   ┐
BlankLines 1      ─    TextLine ""                   │
                  ┌    TextLine "Warnier and Orr…"   │
                  │    TextLine "output hierarchy…"  │
Paragraph P1      ┤    TextLine "easier to…"         │
                  │    TextLine "paragraph break…"   ├    Page 1
                  └    TextLine "under the old…"     │    10 lines = h
                  ┌    GapLine                       │
                  │    TextLine "Every justified…"   │
                  │    TextLine "the right margin…"  ┘    ◄ yield Page 1
Paragraph P2      ┤ ┄┄┄┄┄┄┄┄┄┄┄┄ page edge ┄┄┄┄┄┄┄┄┄┄┄┄┄    _paragraph(P2) waits here
                  │    TextLine "paragraph. The…"    ┐
                  │    TextLine "one line and…"      │
                  └    TextLine "river of white…"    │
                  ┌    GapLine                       │
                  │    TextLine "Item  Qty  Price"   ├    Page 2
Unfilled          ┤    TextLine "----------------"   │    8 lines; the break
                  │    TextLine "Apple  10  1.20"    │    ends it early
                  └    TextLine "Pear    4  1.80"    │
PageBreak h=10    ─    PageBreak h=10                ┘    ◄ yield Page 2
Paragraph P3      ┌    TextLine "A new page…"        ┐    Page 3
                  └    TextLine "break."             ┘    ◄ yield Page 3
```

The left brackets are the block tree, and the right brackets are the page tree. The
middle column, the line, is the only part they share. The bracket of P2 crosses the page
edge: P2 is not inside Page 1, and Page 1 is not inside P2. Blocks and pages have their
edges in different places, so one set of nested loops cannot follow both trees. This is
a **structure clash**. The Warnier/Orr answer is
simple: write one program for each tree, and join them with a stream of the part they
share, the line:

```text
blocks ─► typeset() ─► Line stream ─► paginate() ─► pages
          (Block tree)                (Page tree)
```

Each program sees only its own tree. `typesetting.py` does not know pages, and
`pagination.py` does not know paragraphs. In Python the stream is a generator: `typeset`
makes one line and waits until `paginate` asks for the next one. At the page edge above,
it waits inside `_paragraph(P2)`, between two lines of the same paragraph. It is the same
idea as a Unix pipe, `typeset | paginate`.

The line stream has three kinds of line, and each one tells `paginate` what to do:

```text
Line {
    TextLine  (0,1)     a line to print
  ⊕ GapLine   (0,1)     space before a paragraph; dropped at the top of a page
  ⊕ PageBreak (0,1)     start a new page; it may change the page height
}
```

### Part 1: `typeset` (blocks -> lines)

The code has one function for each bracket of the block tree. One detail is worth a look:

```text
Paragraph {
  Gap Line   (0,g)
  Other Line (0,n)      stretched if justify
  Last Line  (1)        never stretched
}
```

The last line of a justified paragraph is special, so the diagram gives it its own place
in the bracket. The code then has its own place for it, and needs no "is this the last
line?" test in the loop:

```python
*others, last = textwrap.wrap(...)
```

### Part 2: `paginate` (lines -> pages)

`paginate` has the shape of the page tree: an outer loop for `Page (0,n)` and an inner
function `_fill_page` for `Line (1,h)`. Three of the rules above come out of the tree
with no extra code:

* An empty stream gives no pages: `Page (0,n)`.
* Two page breaks in a row give one new page, not an empty one: `Line (1,h)`.
* A page break at the end gives no empty last page: `Line (1,h)` again.

### How the generators connect

The stages form a chain: `render(paginate(typeset(read_markup(lines))))`. Only `typeset`
branches. For each block its `match` picks one branch, and some branches hand the work
to a sub-generator with `yield from`:

```text
typeset
├─ Paragraph  ─► yield from _paragraph       generator
│                ├─ yield from _gap_lines    GapLine × g
│                └─ yield TextLine × n       from textwrap.wrap (a list)
├─ Unfilled   ─► yield from _unfilled        generator
│                ├─ yield from _gap_lines    GapLine × g
│                └─ yield TextLine × n
├─ Centered   ─► yield _centered(block)      plain function, 1 line
├─ BlankLines ─► yield TextLine("") × n      inline loop
└─ PageBreak  ─► yield block                 passed on unchanged
```

* **Pull-driven.** The renderer is the sink: nothing runs until it loops over the pages.
  Each `next()` goes down the chain, and each value comes back up, one at a time.
* **One branch at a time.** `typeset` works on one block at a time, so only one path of
  the tree is active, for example `typeset → _paragraph → _gap_lines`. `yield from`
  passes each `next()` straight to the deepest generator on this path.

## Pass 4 - Parse the Input (LRR)

`markup.py` is the read routine: the brick wall between the user's text and the core. It
does three jobs:

* **It keeps the state.** The settings change while the file is read, and they stay
  here. Each block gets a copy of its layout, so the core has no state.
* **It parses.** "Parse, don't validate": each piece of text becomes a Value Object, and
  the Value Objects check their own rules in `__post_init__`.
* **It adds line numbers.** The domain raises `DocumentError`. The adapter catches it and
  raises `MarkupError` with the line number, because only the adapter knows line numbers.

The sales report in the article collects *every* bad row. Here we stop at the first
error, as a compiler often does: commands change the settings line by line, so one bad
command can make many later lines look wrong.

## Pass 5 - Format the Output (PPR)

The LOS says *what* comes out. The formatter, the classic *Physical Put Routine*, says
how it *looks*. There are two outbound adapters, and both take the same pages:

* `plain_text.py` adds the `--- Page n ---` heading and footing.
* `json_pages.py` writes the same pages as JSON.

`cli.py` picks one with `--format`. To change the look, change one adapter. To add a new
format, add a new adapter. The core does not change in either case. A renderer is any
function `Callable[[Iterable[Page]], str]`, so no `Protocol` class is needed.

## Pass 6 - Verify: A Place for Every Rule

Warnier checked a design against the output: is there "a place for everything"? Here
every rule and every edge case must have exactly one place in the code:

* **Valid but unusual input** is handled by the structure. The cardinalities of the
  diagrams already say what to do.
* **Invalid input** is rejected once, at the edge of the hexagon (Pass 4).

| Case | What happens | Where |
| --- | --- | --- |
| empty file, or only commands | no pages, empty output | `paginate`: `Page (0,n)` |
| `?page` many times in a row | one new page | `_fill_page`: `Line (1,h)` |
| `?page` at the end of the file | no empty last page | `_fill_page`: `Line (1,h)` |
| gap line at the top of a page | dropped | `_fill_page` |
| paragraph across a page edge | split between the pages | the line stream |
| one long word on a justified line | printed as it is, not stretched | `_stretch` |
| `?blank 0` | nothing | `typeset` |
| word longer than the line | error | `Paragraph.__post_init__` |
| margins or indent outside the paper | error | `Layout.__post_init__` |
| right margin past the paper width | error | `markup.py`: `?margin` |
| unknown command, bad numbers | error | `markup.py`: `_obey_command`, `_numbers` |
| `?center` with no text line after it | error | `markup.py`: `_centered` |
| missing file, or not UTF-8 | `Error: ...`, exit code 1 | `cli.py`: `main` |

All stages are generators, so an error can appear while some pages are already made.
The renderer builds the whole output before anything is printed, so the user never sees
half a document.

## Where We Leave the Classic Method

The method is a way of thinking, not a set of laws. This project uses newer tools where
they say the same thing more clearly:

* **Generators for the clash.** The classic answer was two programs and a file between
  them. A generator is the same stream without the file.
* **The standard library for line breaking.** `textwrap.wrap` breaks a paragraph into
  lines, so we do not write our own line breaker. We keep only the part that is ours:
  justify, and the special last line.
* **Value Objects for the LIF.** The classic method checks every record in the read
  step. Here the types check themselves, so the checks cannot be forgotten.
* **The LIF reuses a type of the LOS.** `PageBreak` is defined in `pages.py` and used in
  both trees. The input is designed from the output, so this is natural.
* **One space after a sentence.** Modern typography, and one rule less.

## Exercises

1. **Widows and orphans.** A good typesetter does not leave the first line of a
   paragraph alone at the bottom of a page. Which tree must change? Hint: the line
   stream needs a little more information, for example "keep with the next line".
2. **An HTML renderer.** Add `adapters/html_pages.py` and register it in `RENDERERS` in
   `cli.py`. Check that no file in `domain/` changes.
3. **A new command.** Add `?right`, which prints the next line against the right margin.
   Follow the steps: a new block in the LIF (`document.py`), a new `case` in the LPS
   (`typesetting.py`) and a new `case` in the inbound adapter (`markup.py`).

## Use It With a Coding Agent

The article is the theory, and this project is the practice. The tool is an
[Agent Skill][skills] in [`skills/warnier-orr-design/`](skills/warnier-orr-design/). It
guides a coding agent through the same six passes: the agent writes the output sample
and the trees first, checks for a structure clash, and stops for your approval before it
writes code.

The skill covers the structure inside one use case. It takes its names from your domain
language and follows your project's architecture and style, so it works next to your own
instructions for those layers.

Install it for one repository, or for all your projects:

```bash
cp -r skills/warnier-orr-design <your-repo>/.github/skills/   # one repository
cp -r skills/warnier-orr-design ~/.copilot/skills/            # all projects
```

Other agents that support Agent Skills use the same `SKILL.md` format.

The agent loads the skill when a task produces or transforms structured data. You can
also ask for it by name: "Use the warnier-orr-design skill for this ticket."

## Project Layout

```text
skills/
└── warnier-orr-design/    # the Agent Skill (see "Use It With a Coding Agent")
    ├── SKILL.md           # the phases, checks, and stop points
    ├── LICENSE.txt        # MIT, so the folder can be copied as is
    └── references/        # marks, clashes, and the sales report example
resources/
├── example-page.txt       # the demo input
└── broken.txt             # an input with one mistake (just demo-error)
src/printers_devil/
├── domain/                # the core: pure Python, no I/O
│   ├── pages.py           # Pass 1 - LOS: Page, and the Line stream
│   ├── document.py        # Pass 2 - LIF: Layout and the Block types
│   ├── typesetting.py     # Pass 3 - LPS, part 1: blocks -> lines
│   ├── pagination.py      # Pass 3 - LPS, part 2: lines -> pages (the structure clash)
│   └── errors.py          # DocumentError
├── adapters/              # the edge of the hexagon
│   ├── markup.py          # Pass 4 - LRR: "?command" text -> blocks
│   ├── plain_text.py      # Pass 5 - PPR: pages -> text
│   └── json_pages.py      # Pass 5 - PPR: pages -> JSON
└── cli.py                 # the shell and composition root
```

This is an illustrative example, so it has no tests. Run `just demo`, `just demo-json`
and `just demo-error` to see it work.

## Credits

* The method: [Why Warnier/Orr Thinking Still Matters][post].
* The problem: Charles Wetherell, *Automatic Text Formatting*, from
  [*Etudes for Programmers*][etudes] (1978).

## License

The code and the Agent Skill are under the [MIT License](../../LICENSE). The text of
this README is under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
