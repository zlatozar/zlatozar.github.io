"""Pass 2 - Derive the input (LIF): the ideal input for the LOS.

    Document (1) {
      Block (0,n) {
          Paragraph  (0,1) { Word (1,n) }     filled or justified
        ⊕ Unfilled   (0,1) { Line (1,n) }     printed as typed
        ⊕ Centered   (0,1)                    one line in the middle
        ⊕ BlankLines (0,1)                    empty lines, kept at the top of a page
        ⊕ PageBreak  (0,1)                    start a new page (defined in pages.py)
      }
    }

Each text block carries its own Layout, so typeset is stateless.

Modern Python: blocks are always-valid Value Objects, so the LPS has no checks.
"""

from dataclasses import dataclass

from printers_devil.domain.errors import DocumentError
from printers_devil.domain.pages import PageBreak


@dataclass(frozen=True, slots=True)
class Layout:
    """Where the text goes on a line. Columns start at 1; "·" is a space.

    Example: Layout(left=3, right=20, indent=2, gap=1):

        column   12345678901234567890
                                         <- gap: 1 empty line
                 ····Warnier and Orr     <- first line: left + indent = column 5
                 ··showed that output    <- other lines: left = column 3
                 ··comes first.          <- no line goes past right = column 20

    A negative indent gives a hanging first line: it starts left of the margin.
    """

    left: int
    right: int
    indent: int
    gap: int

    def __post_init__(self) -> None:
        if not 1 <= self.left <= self.right:
            raise DocumentError(f"margins {self.left} {self.right} are wrong: need 1 <= left <= right.")
        first_column = self.left + self.indent
        if not 1 <= first_column <= self.right:
            raise DocumentError(f"indent {self.indent} puts the first line at column {first_column}.")
        if self.gap < 0:
            raise DocumentError(f"paragraph gap must be 0 or more, got {self.gap}.")

    @property
    def width(self) -> int:
        """Columns from left to right, both included."""
        return self.right - self.left + 1

    def room(self, first: bool) -> int:
        """Columns for text on a line; the indent counts on the first line only."""
        return self.width - self.indent if first else self.width


@dataclass(frozen=True, slots=True)
class Paragraph:
    """Words to fill into lines. Example: Paragraph(layout, ("Warnier", "and", "Orr"), justify=True)."""

    layout: Layout
    words: tuple[str, ...]  # (1,n)
    justify: bool = False

    def __post_init__(self) -> None:
        if not self.words:
            raise DocumentError("a paragraph needs at least one word (1,n).")
        # A word can land on any line, so it must fit on the shortest one.
        room = min(self.layout.room(first=True), self.layout.room(first=False))
        for word in self.words:
            if len(word.split()) != 1:
                raise DocumentError(f"{word!r} is not one word: a word has no spaces.")
            if len(word) > room:
                raise DocumentError(f"word {word!r} is {len(word)} columns, but a line has room for {room}.")


@dataclass(frozen=True, slots=True)
class Unfilled:
    """Lines printed as they are typed, like a table. Example: Unfilled(layout, ("Item  Qty", "", "Pear    4"))."""

    layout: Layout
    lines: tuple[str, ...]  # (1,n); "" is an empty line

    def __post_init__(self) -> None:
        if not self.lines:
            raise DocumentError("an unfilled block needs at least one line (1,n).")
        for number, line in enumerate(self.lines):
            room = self.layout.room(first=number == 0)
            if len(line) > room:
                raise DocumentError(f"line {line!r} is {len(line)} columns, but there is room for {room}.")


@dataclass(frozen=True, slots=True)
class Centered:
    """One line in the middle between the margins. Example: Centered(layout, "PRINTER'S DEVIL")."""

    layout: Layout
    text: str

    def __post_init__(self) -> None:
        if len(self.text) > self.layout.width:
            message = f"centered text is {len(self.text)} columns, but the margins allow {self.layout.width}."
            raise DocumentError(message)


@dataclass(frozen=True, slots=True)
class BlankLines:
    count: int  # (0,n)

    def __post_init__(self) -> None:
        if self.count < 0:
            raise DocumentError(f"blank line count must be 0 or more, got {self.count}.")


type Block = Paragraph | Unfilled | Centered | BlankLines | PageBreak  # ⊕
