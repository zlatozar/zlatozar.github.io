"""The core of the hexagon: the logical structures (LOS, LIF) and the process (LPS).

Pure: no I/O, no "?commands", no imports from adapters. Read in pass order:
pages.py (LOS) -> document.py (LIF) -> typesetting.py, pagination.py (LPS).
"""
