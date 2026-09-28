# ADR-0018: Privacy policy on GitHub Pages

- **Status:** Accepted
- **Date:** 2026-09-28
- **Related:** PRD-0002 (RELE-05, OSS-05), ADR-0003, ADR-0015

## Context

Play needs the privacy policy (`docs/privacy-policy.md`) at a stable public HTTPS
URL, and Play Console stores that URL (ADR-0015). The repository is going public
(OSS-05) before the Play Console work, and will be renamed `simmmarken` →
`simmarken` just before that. GitHub does not redirect Pages URLs after a rename.

GitHub Pages builds a folder with Jekyll and the `github-pages` plugin set. By
default it would:

- serve `privacy-policy.md` at `/privacy-policy.html` (and `/privacy-policy`),
  but not at `/privacy-policy/`, so the URL would depend on Jekyll's filename
  handling;
- render *every* Markdown file under `docs/` (ADRs, PRDs, extraction notes,
  agent docs) through Liquid, so a single `{{ }}` or `{% %}` in any of them
  would fail the whole Pages build and take the policy offline.

## Decision

- Pages serves from `main` → `/docs`. The policy lives at
  `https://flyhard.github.io/<repo>/privacy-policy/`.
- `docs/privacy-policy.md` declares `permalink: /privacy-policy/` in its front
  matter. This path is permanent.
- `docs/_config.yml` publishes only the privacy policy: it excludes all
  Markdown (`*.md`) and the `adr/`, `agents/`, `extraction/`, `images/` and
  `prd/` folders, and re-includes `privacy-policy.md`. A new page must be added
  to `include` on purpose.
- `docs/_config.yml` sets no `url`, `baseurl` or `repository`; Pages derives
  them from the repository, so a rename needs no config change.
- The default Pages theme (Primer) is used; no custom layouts or plugins.

## Alternatives considered

- **No config, rely on Pages defaults** — the URL would be `/privacy-policy.html`
  or extension-less, and any Liquid-looking text elsewhere in `docs/` could break
  the build.
- **Publish all of `docs/`** — ADRs and PRDs are already readable on GitHub;
  publishing them adds nothing and ties the policy's availability to their
  content.
- **Separate public repository for the policy** — only needed if the listing had
  to go live before the repository is public; it no longer does.

## Consequences

- The URL contains the repository name. The rename happens before the URL is
  entered in Play Console; any later rename breaks it (Pages URLs are not
  redirected) and means updating the Console.
- Other docs stay on github.com only; the site root (`/`) has no page.
- Verified with a local `jekyll build` using the `github-pages` gem; a broken
  Liquid tag in an ADR, PRD, extraction note or new top-level Markdown file no
  longer affects the build.
