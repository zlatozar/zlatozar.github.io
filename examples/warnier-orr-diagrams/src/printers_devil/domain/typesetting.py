"""Pass 3 - Design the process (LPS), part 1: blocks -> line stream.

typeset knows blocks, not pages: a block may cross a page edge (see pagination.py).

    typeset (1) {
      Block (0,n) {
          Paragraph (0,1) {
            Gap Line   (0,g)          g = layout.gap
            Other Line (0,n)          stretched if justify
            Last Line  (1)            never stretched
          }
        ⊕ Unfilled (0,1) {
            Gap Line (0,g)
            Line     (1,n)            printed as typed
          }
        ⊕ Centered   (0,1)            one line
        ⊕ BlankLines (0,1) {
            Empty Line (0,n)
          }
        ⊕ PageBreak  (0,1)            passed on to paginate
      }
    }
"""

import textwrap
from collections.abc import Iterable, Iterator
from typing import assert_never

from printers_devil.domain.document import BlankLines, Block, Centered, Layout, Paragraph, Unfilled
from printers_devil.domain.pages import GapLine, Line, PageBreak, TextLine


def typeset(blocks: Iterable[Block]) -> Iterator[Line]:
    """A bind (flatMap): each block becomes its own lines, in order."""
    for block in blocks:  # Block (0,n)
        match block:  # ⊕
            case Paragraph():
                yield from _paragraph(block)
            case Unfilled():
                yield from _unfilled(block)
            case Centered():
                yield _centered(block)
            case BlankLines(count=count):
                for _ in range(count):  # Empty Line (0,n)
                    yield TextLine("")
            case PageBreak():
                yield block  # a Block that is also a Line
            case _:
                assert_never(block)  # a new Block type with no case fails type checking


def _paragraph(paragraph: Paragraph) -> Iterator[Line]:
    """Paragraph: Gap Line (0, g), Other Line (0, n), Last Line (1)."""
    layout = paragraph.layout
    yield from _gap_lines(layout)
    # Modern Python: textwrap breaks the lines; only justify and the last line are ours.
    *others, last = textwrap.wrap(  # at least one line: Word (1,n)
        " ".join(paragraph.words),
        width=layout.right,  # textwrap's width includes the indent strings, which hold the left margin
        initial_indent=_lead(layout, first=True),
        subsequent_indent=_lead(layout, first=False),
        break_long_words=False,  # words are never broken; Paragraph checks that each one fits
        break_on_hyphens=False,  # a word is the text between spaces, hyphens included
    )
    for number, line in enumerate(others):  # Other Line (0,n)
        yield TextLine(_stretch(line, layout, number) if paragraph.justify else line)
    yield TextLine(last)  # Last Line (1): never stretched


def _unfilled(unfilled: Unfilled) -> Iterator[Line]:
    """Unfilled: Gap Line (0, g), then Line (1, n), each printed as typed.

    Example (left 3, indent 2): ("Item  Qty", "", "Pear  4") -> "    Item  Qty", "", "  Pear  4"
    """
    layout = unfilled.layout
    yield from _gap_lines(layout)
    for number, text in enumerate(unfilled.lines):  # Line (1,n)
        yield TextLine(_lead(layout, first=number == 0) + text if text else "")  # "" stays "": no trailing spaces


def _centered(centered: Centered) -> TextLine:
    """Centered: one line in the middle of the margins.

    Example (margins 1-20): "TITLE" has 5 columns, 15 are free -> 7 spaces + "TITLE".
    """
    layout = centered.layout
    pad = layout.left - 1 + (layout.width - len(centered.text)) // 2
    return TextLine(" " * pad + centered.text)


def _stretch(line: str, layout: Layout, number: int) -> str:
    """Justify: widen the gaps until the line ends at the right margin.

    The left-over spaces go to the left gaps on even lines and to the right gaps on odd
    lines, so no "river" of white space runs down one side.

        words "a b c d", room 12: 8 spaces in 3 gaps, 2 each, 2 left over
        even:  a   b   c  d     gaps 3 3 2
        odd:   a  b   c   d     gaps 2 3 3
    """
    first = number == 0
    lead = _lead(layout, first)
    words = line[len(lead) :].split()
    gaps = len(words) - 1
    if gaps == 0:  # one word: nothing to stretch
        return line

    spaces = layout.room(first) - sum(len(word) for word in words)
    each, left_over = divmod(spaces, gaps)
    wide = range(left_over) if number % 2 == 0 else range(gaps - left_over, gaps)
    widths = [each + 1 if gap in wide else each for gap in range(gaps)]
    return lead + words[0] + "".join(" " * width + word for width, word in zip(widths, words[1:], strict=True))


def _gap_lines(layout: Layout) -> Iterator[GapLine]:
    """Gap Line (0, g): space before a block. Pagination drops it at the top of a page."""
    for _ in range(layout.gap):
        yield GapLine()


def _lead(layout: Layout, first: bool) -> str:
    """Spaces before the text: the left margin, plus the indent on the first line."""
    return " " * (layout.left - 1 + (layout.indent if first else 0))
