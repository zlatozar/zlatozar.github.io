"""The one error type of the core."""


class DocumentError(ValueError):
    """A domain rule is broken. The message has no line number; markup.py adds one (MarkupError)."""
