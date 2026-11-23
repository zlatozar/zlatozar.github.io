"""Pass 5 - Format the output (PPR), an outbound adapter: pages -> plain text.

    --- Page 1 ---
       Warnier and Orr showed that
    output comes first.
    --- End Page 1 ---

    --- Page 2 ---
    ...

The heading and footing are physical, so they live only here: the page height counts
only the body lines.
"""

from collections.abc import Iterable

from printers_devil.domain.pages import Page


def render_text(pages: Iterable[Page]) -> str:
    """Example: [Page(1, ("Hello",))] -> "--- Page 1 ---\\nHello\\n--- End Page 1 ---"."""
    parts: list[str] = []
    for page in pages:  # Page (0,n)
        if parts:
            parts.append("")  # an empty line between two pages
        parts.append(f"--- Page {page.number} ---")  # Begin Page
        parts.extend(page.lines)  # Line (1,h)
        parts.append(f"--- End Page {page.number} ---")  # End Page
    return "\n".join(parts)
