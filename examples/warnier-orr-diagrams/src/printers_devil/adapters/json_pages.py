"""Pass 5 - Format the output (PPR), a second outbound adapter: pages -> JSON.

The shape, shortened here (json.dumps puts each item on its own row):

    {
      "pages": [
        {"number": 1, "lines": ["   Warnier and Orr showed that", "output comes first."]}
      ]
    }
"""

import json
from collections.abc import Iterable

from printers_devil.domain.pages import Page

JSON_INDENT = 2


def render_json(pages: Iterable[Page]) -> str:
    document = {"pages": [{"number": page.number, "lines": list(page.lines)} for page in pages]}
    return json.dumps(document, indent=JSON_INDENT, ensure_ascii=False)
