"""Pass 4 - Parse the input (LRR), the inbound adapter: markup lines -> blocks (the LIF).

This adapter owns what the core must not know: the "?command" syntax, the settings and
the line numbers. The physical input:

    Markup File (1) {
      Source Line (0,n) {
          Command Line (0,1)    "?margin 3 54"
        ⊕ Text Line    (0,1)    "Warnier and Orr showed"
        ⊕ Empty Line   (0,1)    ""
      }
    }

A block starts and ends at a line edge, so there is no clash here: one loop nest follows
both trees.

    read_markup (1) {
      Block (0,n) {
          Command    (0,1)    change the settings; some commands also make a block
        ⊕ Unfilled   (0,1)    in unfilled mode: every line until the next command
        ⊕ Empty Line (0,1)    skipped: it only ends a paragraph
        ⊕ Paragraph  (0,1)    Text Line (1,n) { Word (1,n) }
      }
    }
"""

from collections.abc import Iterable, Iterator
from dataclasses import dataclass, replace
from enum import Enum

from printers_devil.domain.document import BlankLines, Block, Centered, Layout, Paragraph, Unfilled
from printers_devil.domain.errors import DocumentError
from printers_devil.domain.pages import DEFAULT_PAGE_HEIGHT, PageBreak

COMMAND_MARK = "?"
DEFAULT_PAPER_WIDTH = 72
DEFAULT_INDENT = 3
DEFAULT_LAYOUT = Layout(left=1, right=DEFAULT_PAPER_WIDTH, indent=DEFAULT_INDENT, gap=0)


class MarkupError(Exception):
    def __init__(self, line_number: int, message: str) -> None:
        super().__init__(f"Line {line_number}: {message}")
        self.line_number = line_number


class Mode(Enum):
    FILL = "fill"
    JUSTIFY = "justify"
    UNFILLED = "unfilled"


@dataclass(frozen=True, slots=True)
class Settings:
    """The settings now in use. A command makes new settings; a block copies the layout.

    Example: after "?margin 3 54" and "?mode justify" the settings are
        Settings(page_height=40, paper_width=72,
                 layout=Layout(left=3, right=54, indent=3, gap=0), mode=Mode.JUSTIFY)
    """

    page_height: int = DEFAULT_PAGE_HEIGHT
    paper_width: int = DEFAULT_PAPER_WIDTH
    layout: Layout = DEFAULT_LAYOUT
    mode: Mode = Mode.FILL


def read_markup(lines: Iterable[str]) -> Iterator[Block]:
    """Yield the blocks one at a time; the first mistake raises MarkupError."""
    source = _Source(lines)
    settings = Settings()
    while (line := source.line) is not None:  # Block (0,n)
        block: Block | None = None
        try:
            # The order of the tests matters: in unfilled mode an empty line is kept, not skipped.
            if _is_command(line.text):  # Command
                settings, block = _obey_command(line, source, settings)
            elif settings.mode is Mode.UNFILLED:  # ⊕ Unfilled
                block = _unfilled(source, settings)
            elif _is_empty(line.text):  # ⊕ Empty Line
                source.advance()
            else:  # ⊕ Paragraph
                block = _paragraph(source, settings)
        except DocumentError as error:
            raise MarkupError(line.number, str(error)) from error  # the line where the block starts
        if block is not None:
            yield block


# Source lines with a look-ahead ─────────────────────────────────


@dataclass(frozen=True, slots=True)
class _SourceLine:
    """One line of the markup file. Example: _SourceLine(number=2, text="?margin 3 54")."""

    number: int
    text: str


class _Source:
    """One-line look-ahead: `line` is the next unused line, or None at the end of the file."""

    def __init__(self, lines: Iterable[str]) -> None:
        self._lines = enumerate(lines, start=1)
        self.line: _SourceLine | None = None
        self.advance()

    def advance(self) -> None:
        """The current line is used up: look at the next one."""
        item = next(self._lines, None)
        self.line = None if item is None else _SourceLine(number=item[0], text=item[1])


def _is_command(text: str) -> bool:
    return text.startswith(COMMAND_MARK)


def _is_empty(text: str) -> bool:
    return not text.strip()


def _is_text(text: str) -> bool:
    return not _is_command(text) and not _is_empty(text)


# Block choices ──────────────────────────────────────────────────


def _obey_command(line: _SourceLine, source: _Source, settings: Settings) -> tuple[Settings, Block | None]:
    """Command: return the new settings and, for some commands, one block.

    A command line is never part of a paragraph, so every command ends the paragraph
    before it. That paragraph keeps the old settings.
    """
    name, *args = line.text.removeprefix(COMMAND_MARK).split() or [""]
    source.advance()  # the command line is used up
    match name:
        case "papersize":
            height, width = _numbers(line, args, 2, 2)
            if height < 1 or width < 1:
                raise MarkupError(line.number, "paper size must be at least 1 x 1.")
            # replace() calls __post_init__: every new Layout checks its own rules.
            layout = replace(settings.layout, left=1, right=width)  # new paper: the margins reset
            new_settings = replace(settings, page_height=height, paper_width=width, layout=layout)
            return new_settings, PageBreak(height)  # a new paper size starts a new page
        case "margin":
            left, right = _numbers(line, args, 2, 2)
            if right > settings.paper_width:  # Layout does not know the paper width
                message = f"right margin {right} is past the paper width {settings.paper_width}."
                raise MarkupError(line.number, message)
            return replace(settings, layout=replace(settings.layout, left=left, right=right)), None
        case "mode":
            return replace(settings, mode=_mode(line, args)), None
        case "paragraph":
            values = _numbers(line, args, 0, 2)  # ?paragraph [indent [gap]]
            indent = values[0] if len(values) > 0 else settings.layout.indent
            gap = values[1] if len(values) > 1 else settings.layout.gap
            return replace(settings, layout=replace(settings.layout, indent=indent, gap=gap)), None
        case "center":
            _numbers(line, args, 0, 0)  # rejects any argument
            return settings, _centered(line, source, settings)
        case "blank":
            values = _numbers(line, args, 0, 1)
            count = values[0] if values else 1  # "?blank" is "?blank 1"
            return settings, BlankLines(count)
        case "page":
            _numbers(line, args, 0, 0)
            return settings, PageBreak(settings.page_height)
        case "":
            raise MarkupError(line.number, "a command name is missing after '?'.")
        case _:
            raise MarkupError(line.number, f"unknown command ?{name}.")


def _centered(command: _SourceLine, source: _Source, settings: Settings) -> Centered:
    """Centered: "?center" takes the next line, and it must be a text line.

    Example: "?center" + "  PRINTER'S DEVIL" -> Centered(layout, "PRINTER'S DEVIL")
    """
    line = source.line
    if line is None or not _is_text(line.text):
        raise MarkupError(command.number, "?center must be followed by a text line.")
    source.advance()
    return Centered(settings.layout, line.text.strip())


def _unfilled(source: _Source, settings: Settings) -> Unfilled:
    """Unfilled: every line until the next command. Empty lines are kept.

    Example: "Item  Qty" + "" + "Pear    4" -> Unfilled(layout, ("Item  Qty", "", "Pear    4"))
    """
    lines: list[str] = []
    while (line := source.line) is not None and not _is_command(line.text):  # Line (1,n)
        lines.append(line.text.rstrip())
        source.advance()
    return Unfilled(settings.layout, tuple(lines))


def _paragraph(source: _Source, settings: Settings) -> Paragraph:
    """Paragraph: text lines until an empty line, a command, or the end.

    Example: "Warnier and Orr" + "showed that" -> words ("Warnier", "and", "Orr", "showed", "that")
    """
    words: list[str] = []
    while (line := source.line) is not None and _is_text(line.text):  # Text Line (1,n)
        words.extend(line.text.split())  # Word (1,n)
        source.advance()
    return Paragraph(settings.layout, tuple(words), justify=settings.mode is Mode.JUSTIFY)


# Command arguments ──────────────────────────────────────────────


def _numbers(line: _SourceLine, args: list[str], min_count: int, max_count: int) -> list[int]:
    """Check the argument count, then parse each argument as an int."""
    if not min_count <= len(args) <= max_count:
        expected = f"{min_count}" if min_count == max_count else f"{min_count} to {max_count}"
        raise MarkupError(line.number, f"{line.text!r} needs {expected} number(s), got {len(args)}.")
    try:
        return [int(arg) for arg in args]
    except ValueError:
        raise MarkupError(line.number, f"{line.text!r} needs whole numbers.") from None


def _mode(line: _SourceLine, args: list[str]) -> Mode:
    if len(args) != 1:
        raise MarkupError(line.number, f"?mode needs 1 word, got {len(args)}.")
    try:
        return Mode(args[0])
    except ValueError:
        raise MarkupError(line.number, f"?mode must be fill, justify or unfilled, got {args[0]!r}.") from None
