# Structure Clashes

A structure clash happens when the input tree and the output tree do not line up. Do not
force one loop to serve both trees. Use two programs joined by a stream: the first
flattens one tree into a stream, and the second builds the other tree from that stream.

## The Check

1. Take one pair of trees: the real input and the output tree, or the output tree and
   the physical tree (pages, batches, chunks).
2. For each group in the second tree, ask: which group of the first tree holds all of
   its items? If none does, ask: do its items come together, or scattered among the
   items of other groups?
3. Ask about order: do the groups, and the items inside each group, come in the order
   the second tree needs?
4. Read the verdicts from the table. A pair can have more than one: a grouping clash
   often comes with an ordering clash.

| What you find | Verdict | What the stream carries |
| --- | --- | --- |
| Each group sits inside one group of the first tree, in the needed order | no clash | nothing extra; the step is a bind |
| A group's items come together, but cut across groups of the first tree | boundary clash | markers for the boundaries |
| A group's items are scattered, and nothing marks where a group ends | grouping clash | a group key |
| A group's items are scattered, and each group has its own start and end | interleaving clash | a group key |
| The groups, or the items inside a group, come in another order | ordering clash | a sort key |

A *bind* turns each input node into its own output items and joins the results in order
(`yield from` in Python, `flatMap` in many other languages). If you can write the step as
a bind, the structures fit. If an output group needs items from more than one input
node, the step needs state, and it becomes its own program.

Grouping and interleaving clashes look alike: in both, items of many groups come mixed.
Ask: can a group be finished before the input ends?

* No, nothing marks the end of a group: a grouping clash. Read the whole input, and put
  each item into the bucket of its group key.
* Yes, each group has its own start and end: an interleaving clash. Keep one state per
  open group, and output each group when it ends, in the order the groups end.

Grouping and sorting are different needs. Ask: where does the order of the output groups
come from?

* From outside the data, such as a master list, or from the order in which each group
  first appears: grouping is enough. A map from key to bucket does it, with no sort.
* From a value in the data, such as a customer id, a date, or a total: that is also an
  ordering clash, and it needs a sort. The same holds for the items inside a group.

A sort by the group key also brings each group together, so one sort can solve a
grouping clash and an ordering clash at once. It is also the way out when the buckets do
not fit in memory: sort before the program, and each group arrives in one piece.

Any interleaving clash can also be solved by grouping or sorting on the group key.
Choose that when the program can wait for the end of the input, or when the output needs
key order. Keep one state per open group only when it cannot wait: the input never
stops, or each group is needed as soon as it ends.

A pipeline can have clashes in more than one pair of trees. Handle each pair in its own
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
* **Grouping clash:** nothing comes out until the whole input is read, because the last
  item may belong to the first group. Memory holds every item in its bucket, or one
  running value per group when the output needs only totals, as SQL `GROUP BY` does.
  Ask how large the input can grow, and where the order of the groups comes from. If
  the buckets do not fit in memory, sort by the group key before the program: an
  external sort, or `ORDER BY` in the database. Then each group arrives in one piece,
  and the step can stream.
* **Ordering clash:** ask for the sort key, its direction, and what breaks ties. To sort
  the groups, the program must hold all of them, so nothing comes out until the whole
  input is read. Ask how large the input can grow; if it does not fit in memory, sort
  outside the program, as above. To sort only the items inside each group, it holds one
  group at a time. When the output needs only the first N, keep the best N so far, not
  every item.
* **Interleaving clash:** the steps run together, but the state grows with the number of
  open groups. Ask how a group ends: an end record, a timeout, or the end of the input.
  Ask what happens to groups still open when the input stops: report them as
  incomplete, or drop them. Without end rules, the state only grows. The groups come out
  in the order they end; if the output needs another order, there is also an ordering
  clash.

When the steps run together, ask one more question: may the user see part of the output
when a later item is bad? If not, one edge holds everything back until the input is
read. The parser can collect every bad row first, as in
[sales-report.md](sales-report.md), or the formatter can build the whole text before it
writes any of it.

## Keep the Stream Lazy

The second program pulls, and the first runs only as far as the next item needs. Between
pulls, the first keeps its place. Use the language's lazy sequence for the stream: a
generator, an iterator, or a lazy sequence type.

Two rules follow:

* Never collect the stream between two steps. A list in the middle makes the pipeline
  eager. The output stays the same, so no golden test sees it. Only the laziness test
  from phase 6 does.
* Read a stream only once. A lazy stream does not store its items: it makes each item
  once, and after the last one it is empty. A second loop over it gets no items and
  raises no error, so the bug shows only as missing output:

  ```python
  lines = typeset(blocks)          # a lazy stream: nothing is computed yet
  total = sum(1 for _ in lines)    # reads every line and uses the stream up
  pages = list(paginate(lines))    # BUG: second read of lines; gets [] with no error
  ```

  The usual case is "page 1 of N": the step needs N before it prints page 1. If a step
  needs the items twice, make the stream again from its source, or collect it into a
  list on purpose and accept the memory cost.

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

page, batch, chunk, limit, at most N, per N, group by, sorted by, order by, ranked, top N,
per customer, session, topic, partition, window, split, merge, continued.

A hint is not a verdict. Always run the check.

## Worked Examples

Each example names what the clash needs: grouping, sorting, both, or neither.

**Report to pages: boundary clash; neither.** Input: stores, each with transactions.
Output: pages of 54 lines. One page can hold the end of one store and the start of the
next, so a page cuts across stores. The stream is lines, and each line carries its
store, for the "continued" header.

**Statements where each customer starts a new page: no clash; neither.** Output:
customers, each with their postings. Physical: pages of 50 lines. A customer can fill
several pages, but no page holds two customers, so each page sits inside one customer.
The hint words are there (page, at most 50, continued), but the check says no clash. Each
customer becomes its own pages, and the formatter joins them; the page number is its
only running state.

**Stores to lines: no clash; neither.** Each store becomes its own lines, and the results
are joined. Every output line belongs to one store. This is a bind.

**Orders from a paged API: boundary clash; neither.** Input: pages of 100 order lines.
Output: one invoice per order. An order can start on one page and end on the next. The
stream is order lines, and each line carries its order id. If the API does not keep the
lines of one order together, there is also a grouping clash: put the lines into buckets
by order id. Sort only if the invoices must come in order-id order.

**Sales rows to a report by store: grouping clash; grouping only.** Input: the month's
transaction rows of all stores, mixed in one file. Output: stores in the order of the
store list, each with its transactions in file order. Put each row into the bucket of its
store id. The store list gives the order of the groups, and the file gives the order
inside each group, so nothing is sorted. See [sales-report.md](sales-report.md).

**Events by time to a monthly report by customer: grouping and ordering clash; both.**
Input: the month's payment events in time order. Output: customers in id order, each
with their payments in time order. The events of one customer are scattered, and the
customer order is a value in the data. One sort by customer id, then time, does both:
each customer comes in one piece, in key order. The report is built after the month is
closed, so the program can wait for the whole input. Cost question: how many events can
a month have? If they fit in memory, sort in the program; if not, use `ORDER BY` in the
database.

**Orders by time to the 20 largest orders: ordering clash; sorting only.** Input: the
day's orders in time order. Output: the 20 largest, by amount. Each order is one item,
so there is nothing to group. The sort key is the amount, largest first, and the order
id breaks ties. Only the first 20 are needed, so keep the best 20 so far, not every
order.

**Sessions in a live log: interleaving clash; grouping only.** Input: a log stream where
the lines of many user sessions are mixed. Output: one summary as soon as each session
ends, in the order the sessions end. Many sessions are open at once. Each line carries
its session id as the group key, and the second program keeps one state per open
session. Cost questions: does every session end with a logout line, or is a timeout
needed? What happens to sessions still open when the stream stops?

**Paragraphs to chunks of at most N tokens: boundary clash; neither.** Input: documents
with paragraphs. Output: chunks for an embedding model. A long paragraph can be split
across chunks. The stream is sentences, and each sentence carries its document and
paragraph, so each chunk can say where it came from.
