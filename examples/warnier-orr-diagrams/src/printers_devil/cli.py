"""Command line entry point: `printers-devil [--format text|json] FILE`.

The imperative shell and the composition root: the only module that wires the adapters
to the core.
"""

import argparse
import sys
from collections.abc import Callable, Iterable
from pathlib import Path

from printers_devil.adapters.json_pages import render_json
from printers_devil.adapters.markup import MarkupError, read_markup
from printers_devil.adapters.plain_text import render_text
from printers_devil.domain.pages import Page
from printers_devil.domain.pagination import paginate
from printers_devil.domain.typesetting import typeset

type Renderer = Callable[[Iterable[Page]], str]  # the shape of every outbound adapter

RENDERERS: dict[str, Renderer] = {"text": render_text, "json": render_json}


def main(argv: list[str] | None = None) -> int:
    args = _parse_args(argv)
    try:
        source_lines = Path(args.input_file).read_text(encoding="utf-8").splitlines()
        output = format_document(source_lines, RENDERERS[args.format])
    except (OSError, UnicodeDecodeError, MarkupError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 1
    print(output)
    return 0


def format_document(source_lines: Iterable[str], render: Renderer) -> str:
    """Run the pipeline. The stages are lazy: a MarkupError comes out of render(), before any output."""
    blocks = read_markup(source_lines)  # Pass 4, LRR: markup -> LIF
    lines = typeset(blocks)  # Pass 3, LPS part 1: LIF -> line stream
    pages = paginate(lines)  # Pass 3, LPS part 2: line stream -> LOS
    return render(pages)  # Pass 5, PPR: LOS -> text or JSON


def _parse_args(argv: list[str] | None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Format text with simple PrintersDevil commands.")
    parser.add_argument("input_file", help="path to the markup file")
    parser.add_argument("--format", choices=sorted(RENDERERS), default="text", help="output format (default: text)")
    return parser.parse_args(argv)


if __name__ == "__main__":
    raise SystemExit(main())
