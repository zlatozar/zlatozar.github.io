# Structure Clashes

A structure clash happens when the input tree and the output tree do not line up. Do not
force one loop to serve both trees. Use two programs joined by a stream: the first
flattens one tree into a stream, and the second builds the other tree from that stream.

## The Check

1. Take one pair of trees: the real input and the output tree, or the output tree and
   the physical tree (pages, batches, chunks).
2. For each group in the second tree, ask: which group of the first tree holds all of
   its items?
3. Read the verdict from the table.

| What you find | Verdict | What the stream carries |
| --- | --- | --- |
| Each group sits inside one group of the first tree | no clash | nothing extra; the step is a bind |
| A group cuts across groups of the first tree | boundary clash | markers for the boundaries |
| The same groups come in a different order | ordering clash | a sort key |
| One group of the first tree mixes items of many groups | interleaving clash | a group key |

A *bind* turns each input node into its own output items and joins the results in order
(`yield from` in Python, `flatMap` in many other languages). If you can write the step as
a bind, the structures fit. If an output group needs items from more than one input
node, the step needs state, and it becomes its own program.

Ordering and interleaving clashes look alike: in both, items of many groups come mixed.
Ask: can a group be finished before the input ends?

* No, nothing marks the end of a group: an ordering clash. Read the whole input, then
  output the groups in key order.
* Yes, each group has its own start and end: an interleaving clash. Keep one state per
  open group, and output each group when it ends, in the order the groups end.

Any interleaving clash can also be solved as an ordering clash, by sorting on the group
key. Choose that when the program can wait for the end of the input, or when the output
needs key order. Keep one state per open group only when it cannot wait: the input never
stops, or each group is needed as soon as it ends.

A pipeline can have more than one clash. Handle them one at a time, each with its own
step.

A lookup file, such as a list of names by id, is not a clash. Each output item takes one
row from it by key, and its order does not matter. The parser joins it in.

## What Each Clash Costs

Each kind of clash has a cost. Ask its questions in phase 2, and write the answers in the
clash check of the design note.

* **Boundary clash:** the two steps run together. The second pulls one item at a time,
  and the first keeps its place between pulls (Jackson's *program inversion*). The total
  work is the same as with a list between them, but memory holds one group, and the
  first group costs only its own work, whatever the size of the input.
* **Ordering clash:** nothing comes out until the whole input is read, because the last
  record may belong to the first group. Ask how large the input can grow. If it fits in
  memory, group or sort in the program. If not, sort outside it: an external sort, or
  `ORDER BY` in the database.
* **Interleaving clash:** the steps run together, but the state grows with the number of
  open groups. Ask how a group ends: an end record, a timeout, or the end of the input.
  Ask what happens to groups still open when the input stops: report them as
  incomplete, or drop them. Without end rules, the state only grows.

When the steps run together, ask one more question: may the user see part of the output
when a later item is bad? If not, one edge holds everything back until the input is
read. The parser can collect every bad row first, as in
[sales-report.md](sales-report.md), or the formatter can build the whole text before it
writes any of it.

## Keep the Stream Lazy

The second program pulls, and the first runs only as far as the next item needs. The
stream is a generator in Python and TypeScript, a `Sequence` in Kotlin, and an
`Iterator` in Java and Rust.

Two traps:

* A list between the steps (`list()`, `toList()`, `collect()`) makes the pipeline eager.
  The output stays the same, so no golden test sees it. Only the laziness test from
  phase 6 does.
* A Python or TypeScript generator and a Java `Iterator` can be read only once. A second
  loop over it gets nothing, with no error.

## Finding the Meta Information

List the rules of the second tree. Every rule that names a part of the first tree becomes
a marker or a field in the stream. Put it in the stream type, not in the domain types.

Example: a page that starts in the middle of a store must begin with
`S1 Sofia Central (continued)`. The page rule names the store, so each line in the stream
carries its store.

Two clues in the physical tree:

* **An item the layout repeats**, such as a customer name in every page header. It
  belongs to its logical level, the customer, not to the page. The stream must carry it.
* **A counter that runs across groups**, such as a page number. It is not part of the
  domain. It is state of the step that builds the physical output, whether or not there
  is a clash.

## Words That Hint at a Clash

page, batch, chunk, limit, at most N, per N, group by, sorted by, per customer, session,
topic, partition, window, split, merge, continued.

A hint is not a verdict. Always run the check.

## Worked Examples

**Report to pages: boundary clash.** Input: stores, each with transactions. Output:
pages of 54 lines. One page can hold the end of one store and the start of the next, so
a page cuts across stores. The stream is lines, and each line carries its store, for the
"continued" header.

**Statements where each customer starts a new page: no clash.** Output: customers, each
with their postings. Physical: pages of 50 lines. A customer can fill several pages, but
no page holds two customers, so each page sits inside one customer. The hint words are
there (page, at most 50, continued), but the check says no clash. Each customer becomes
its own pages, and the formatter joins them; the page number is its only running state.

**Stores to lines: no clash.** Each store becomes its own lines, and the results are
joined. Every output line belongs to one store. This is a bind.

**Orders from a paged API: boundary clash.** Input: pages of 100 order lines. Output:
one invoice per order. An order can start on one page and end on the next. The stream
is order lines, and each line carries its order id. If the API does not keep the lines
of one order together, there is also an ordering clash: sort by order id first.

**Events by time to a monthly report by customer: ordering clash.** Input: the month's
payment events in time order. Output: customers, each with their payments. The report is
built after the month is closed, so the whole input can be sorted. The stream carries
the customer id as the sort key; sort, then group. Cost question: how many events can a
month have? If they fit in memory, sort in the program; if not, let the database sort.

**Sessions in a live log: interleaving clash.** Input: a log stream where the lines of
many user sessions are mixed. Output: one summary as soon as each session ends. Many
sessions are open at once. Each line carries its session id as the group key, and the
second program keeps one state per open session. Cost questions: does every session end
with a logout line, or is a timeout needed? What happens to sessions still open when the
stream stops?

**Paragraphs to chunks of at most N tokens: boundary clash.** Input: documents with
paragraphs. Output: chunks for an embedding model. A long paragraph can be split across
chunks. The stream is sentences, and each sentence carries its document and paragraph,
so each chunk can say where it came from.
