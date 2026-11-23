# Copilot Instructions for zlatozar.github.io

This repository hosts the personal engineering blog of **Zlatozar Zhelyazkov**
(**От дума на дума**), built with Jekyll for GitHub Pages.

## Brand & Content Rules

* **Supersedes earlier blog:** Never mention or link to `zlatozar.blogspot.com`
  anywhere on the site. Older posts are being migrated here one by one.
* **No header logo:** Keep the site header brand typographic (`Zlatozar` / `От дума на
  дума`). Do not add an SVG or image logo to the navbar.
* **Images:** Store blog and profile images in `assets/images/` (not the repo root).
* **License:** Code (examples, Agent Skills, layouts, CSS/JS) is MIT (`LICENSE`). Post
  text and diagrams are CC BY 4.0. Personal photos are all rights reserved. Give each new
  Agent Skill `license: MIT` in its front matter and a copy of `LICENSE` as `LICENSE.txt`.

## Writing Style & Voice

* Write in clear, direct, plain-spoken English that fits an experienced non-native
  software engineer.
* Avoid flowery idioms, marketing phrasing, or overly ornate sentences.
* Open technical articles with concrete engineering questions (such as code structure,
  object granularity, or architectural boundaries) rather than historical overviews,
  unless requested otherwise.

## Markdown & Post Conventions

* **Line width:** Wrap Markdown prose lines to **at most 90 characters** (except fenced
  code blocks, ASCII diagrams, tables, URLs, and YAML front matter strings).
* **Post location & naming:** Place posts in `_posts/YYYY-MM-DD-slug.md`. When migrating
  an older post, keep its original publication date in the filename and `date:` field.
* **Front matter:** Include `title`, `subtitle`, `description`, `date`, `categories`, and
  `tags` (plus `example: <slug>` if a companion project exists in `examples/<slug>/`).
  `_config.yml` already sets `layout: post`, `author`, and `permalink: /:title/`.
* **Headings:** Start section headings inside post bodies at `##` (not `#`), because
  `title` is rendered as `<h1>` and the post layout builds the Table of Contents from `##`
  and `###`.

## Python Projects & Code Examples

* **Location & naming:** Place runnable companion projects under `examples/<slug>/`,
  matching the post filename without its `YYYY-MM-DD-` date prefix (e.g.,
  `_posts/2026-11-22-warnier-orr-diagrams.md` maps to `examples/warnier-orr-diagrams/`).
  Keep the directory `kebab-case` and use `snake_case` for any importable Python
  package inside `src/`. Exclude `examples` from Jekyll builds in `_config.yml`.
* **Cross-linking:** Set `example: <slug>` in the post's front matter so
  `_layouts/post.html` renders the header badge and footer card automatically, and link
  back from `examples/<slug>/README.md` to `https://zlatozar.github.io/<slug>/`.
* **Domain-first naming:** Name modules, types, and functions in plain domain language
  rather than methodology or compiler jargon (unless the post is about compilers), and
  keep any Warnier/Orr output hierarchy visible in the code structure.
* **Python version & tooling:** Target Python 3.12+. Use `uv` and `pyproject.toml`
  (with dev dependencies in `[dependency-groups] dev`) for standalone Python projects
  or multi-file examples. Check types with `ty` (a dev dependency, run by `just check`).
* **Automation (`justfile`):** When build, lint, or test steps are more than a single
  command, include a `justfile` with clear recipes (e.g., `lint`, `test`, `run`).
* **Ruff rules:** Follow `target-version = "py312"` and `line-length = 120` with Ruff
  rules `E`, `W`, `F`, `I`, `N`, `B`, `S`, `UP`, `SIM`, `RUF` (allowing `S101`
  `assert` in `**/test_*.py`; skipping high-friction rules like `C4`, `ARG`, `PTH`,
  `TID`).
* **Types & docstrings:** Annotate public function parameters and return values using
  `X | Y` / `str | None` union syntax (not `Optional`). Name unions with the `type`
  statement (`type Shape = Circle | Square`) and end every `match` over a union with
  `case _: assert_never(value)` so a missing case fails type checking. Keep docstrings
  brief—usually one line when type hints already make parameters and return values
  clear—and comment *why* in plain English rather than narrating *what*.
* **Style & reliability:** Write plain, explicit, readable code—avoid magic numbers,
  keep functions focused (SRP/DRY), order public functions before private `_helpers`
  (stepdown order), and test pure logic thoroughly (mocking external I/O).
* **Error handling:** Catch specific exceptions and raise domain errors rather than
  bare `Exception`, and never swallow errors silently. Never hardcode or commit secrets.

## Jekyll, GitHub Pages & Local Preview

* Keep the site 100% compatible with standard GitHub Pages Jekyll 3.10 (`kramdown` GFM,
  `rouge`, `jekyll-feed`, `jekyll-seo-tag`). Do not rely on unsupported third-party Jekyll
  plugins.
* Exclude non-site repo tooling and companion code (`Dockerfile`, `compose.yaml`,
  `justfile`, `Readme.md`, `LICENSE`, `examples`, `.idea`) via `exclude:` in
  `_config.yml`.
* Use `justfile` recipes instead of direct `docker` or `docker compose` commands
  (`just preview`, `just up`, `just up-bg`, `just down`, `just logs`, `just build`,
  `just clean`) to manage the local preview at `http://localhost:4000`.
