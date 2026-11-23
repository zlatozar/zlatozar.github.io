"""Pass 1 - Define the result (LOS): pages of lines.

    Document (1) {
      Page (0,n) {          an empty document gives no pages
        Line (1,h)          h: the page height
      }
    }

No page heading here: it is physical, so it lives in the PPR (adapters/plain_text.py).

A line stream joins typeset and paginate, the two parts of the LPS (see pagination.py).
Next to the text, it carries two markers:

    Line {
        TextLine  (0,1)     a line to print; "" is an empty line
      ⊕ GapLine   (0,1)     marker: an empty line, dropped at the top of a page
      ⊕ PageBreak (0,1)     marker: start a new page; it may change the page height
    }
"""

from dataclasses import dataclass

from printers_devil.domain.errors import DocumentError

DEFAULT_PAGE_HEIGHT = 40  # until a PageBreak sets another height


@dataclass(frozen=True, slots=True)
class TextLine:
    text: str


@dataclass(frozen=True, slots=True)
class GapLine:
    """Marker: paginate drops it at the top of a page."""


@dataclass(frozen=True, slots=True)
class PageBreak:
    """Marker: start a new page; from here on a page has `height` lines."""

    height: int

    def __post_init__(self) -> None:
        if self.height < 1:
            raise DocumentError(f"page height must be 1 or more, got {self.height}.")


type Line = TextLine | GapLine | PageBreak  # ⊕


@dataclass(frozen=True, slots=True)
class Page:
    """One page of output. Example: Page(number=1, lines=("Hello", "", "World"))."""

    number: int
    lines: tuple[str, ...]  # (1,h)

    def __post_init__(self) -> None:
        if not self.lines:
            raise DocumentError(f"page {self.number} needs at least one line (1,h).")
