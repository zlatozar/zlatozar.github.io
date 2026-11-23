"""Pass 3 - Design the process (LPS), part 2: line stream -> pages.

Boundary clash: a block can cross a page edge, so one loop nest cannot follow both the
block tree (LIF) and the page tree (LOS). Two programs, one per tree, are joined by the
line stream: typeset knows only blocks, paginate knows only pages.

    paginate (1) {
      Page (0,n) {
        Begin Page                no lines yet
        Line (1,h) {              h = page height
            TextLine  (0,1)       add the text
          ⊕ GapLine   (0,1)       add "", except at the top of a page
          ⊕ PageBreak (0,1)       end the page; the next page has the new height
        }
        End Page                  yield Page
      }
    }

Modern Python: the stream is a generator, not an intermediate file (program inversion).
"""

import itertools
from collections.abc import Iterable, Iterator
from typing import assert_never

from printers_devil.domain.pages import DEFAULT_PAGE_HEIGHT, GapLine, Line, Page, PageBreak, TextLine


def paginate(lines: Iterable[Line]) -> Iterator[Page]:
    """An unfold: its state is the lines already on the page, and the page height."""
    stream = iter(lines)  # one iterator for all pages: each page starts where the last one stopped
    height = DEFAULT_PAGE_HEIGHT
    for number in itertools.count(start=1):  # Page (0,n)
        body, height = _fill_page(stream, height)
        if not body:  # no lines: the stream has ended
            return
        yield Page(number, tuple(body))  # End Page


def _fill_page(stream: Iterator[Line], height: int) -> tuple[list[str], int]:
    """Return one page of lines ([] at the end of the stream) and the next page height."""
    body: list[str] = []  # Begin Page
    for line in stream:  # Line (1,h)
        match line:  # ⊕
            case TextLine(text=text):
                body.append(text)
            case GapLine() if not body:
                continue  # no gap at the top of a page
            case GapLine():
                body.append("")
            case PageBreak(height=new_height):
                if body:
                    return body, new_height
                height = new_height  # Line (1,h): no empty page; the break only sets the height
            case _:
                assert_never(line)
        if len(body) == height:
            return body, height
    return body, height
