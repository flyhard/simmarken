# Play Console setup runbook

This is the one-time, manual Play Console bootstrap described in
[ADR-0015](adr/0015-play-console-bootstrap.md). It covers PRD-0002
**RELE-01** (remainder), **RELE-02** and **RELE-05**.

Fill in the *Evidence* lines as you go and commit this file. **Never paste
secrets here:** no passwords, private keys or service-account JSON. Certificate
fingerprints, release IDs and dates are fine.

## Status

| Step | Requirement | Done | Date |
|------|-------------|------|------|
| 1. Privacy policy hosted | RELE-05 | [ ] | |
| 2. App record created | RELE-02 | [ ] | |
| 3. Store listing and app content | RELE-05 | [ ] | |
| 4. Play App Signing with existing upload key | RELE-01 | [ ] | |
| 5. First internal-testing release (manual upload) | RELE-02 | [ ] | |
| 6. Service account and `PLAY_SERVICE_ACCOUNT_JSON` | RELE-02 | [ ] | |
| 7. Internal testers | RELE-05 | [ ] | |

---

## 1. Host the privacy policy

The text is in [`docs/privacy-policy.md`](privacy-policy.md). Play needs it at a
stable, public HTTPS URL.

Options:

- **GitHub Pages from `main` → `/docs`** (Settings → Pages). URL:
  `https://flyhard.github.io/simmarken/privacy-policy/`. Pages on a *private*
  repo needs a paid GitHub plan. On the free plan, this works only after the
  repository goes public (OSS-05). `docs/_config.yml` and the policy's
  `permalink` make Pages serve exactly this path and nothing else from `docs/`
  ([ADR-0018](adr/0018-privacy-policy-on-github-pages.md)).
- A separate small public repository (for example `flyhard/simmarken-privacy`)
  with Pages enabled, if the listing must go live before OSS-05.

The URL is stored in Play Console. If the policy moves, update the Console too
(ADR-0015).

- Evidence: URL = `…`, confirmed reachable on `YYYY-MM-DD`

## 2. Create the app record

Play Console → **Create app**:

- App name: **Simmärken**
- Default language: **Swedish – sv-SE**
- App or game: App · Free or paid: Free
- Accept the declarations.

After the first upload (step 5), the package name `se.simmarken` is locked to
this record.

- Evidence: app created `YYYY-MM-DD`

## 3. Store listing and app content

**Main store listing** (sv-SE):

- Short description (≤ 80 characters), for example:
  *Håll koll på barnens simmärken – klarade krav och köpta märken.*
- Full description: what the app does, both catalogs (Svensk Simidrott, SLS),
  works offline, no account.
- App icon 512×512, feature graphic 1024×500, and at least 2 phone screenshots.
  Don't draw these by hand; see [Store graphics](#store-graphics) below.

### Store graphics

`scripts/store-graphics.py` makes the icon and feature graphic from the app's
adaptive launcher icon ([ADR-0018](adr/0017-store-graphics-from-launcher-icon.md)).
It needs Python 3.9+ and `rsvg-convert` (macOS: `brew install librsvg`;
Debian/Ubuntu: `sudo apt-get install librsvg2-bin`). The script checks for both
before doing anything.

```sh
scripts/store-graphics.py generate
# → build/store-graphics/play-icon.png        512×512, 32-bit PNG with alpha → "App icon"
# → build/store-graphics/feature-graphic.png  1024×500, 24-bit PNG, no alpha  → "Feature graphic"
```

Re-run it after any launcher-icon change; the same icon always gives the same
files. The output folder is git-ignored. Commit copies elsewhere only if you
want a fixed reference.

**Phone screenshots** are taken by hand (phone or emulator, 2–8 of them). The
listing is public: **use made-up child names only**, never real ones, and no
real photos. Put them in a folder and check them before uploading:

```sh
scripts/store-graphics.py check-screenshots ~/Desktop/simmarken-screenshots
```

It prints `ok` or `FAIL` with the reason for each file (too small, too large,
aspect ratio over 2:1, alpha channel, over 8 MB) and fails if there are fewer
than 2 or more than 8. Screenshots under 1080 px get a warning: Play accepts
them but won't feature them.

**App content** (Policy → App content):

| Section | Answer |
|---------|--------|
| Privacy policy | URL from step 1 |
| Ads | No ads |
| App access | All functionality available without special access |
| Content rating (IARC) | Answer honestly: no violence, no user-generated content, no data sharing, no purchases. Expected **Everyone / PEGI 3** |
| Target audience | Parents (18+). The app is not directed at children. |
| Data safety | **No data collected, no data shared.** Data is stored on the device only. Export is user-initiated through the share sheet. |
| Government apps / financial features / health | Not applicable |

- Evidence: content rating = `…`, data safety submitted `YYYY-MM-DD`

## 4. Play App Signing with the existing upload key

The upload key already exists locally (see `scripts/generate-upload-keystore.sh`).
Its SHA-256 is in the gitignored `keystore-fingerprint.md`.

1. Choose **Use Google-generated app signing key** (the default). Our local key
   becomes the **upload key**.
2. If Play asks for an upload certificate, export it:
   ```sh
   keytool -export -rfc -keystore <upload.jks> -alias <alias> -file upload_certificate.pem
   ```
   Don't commit `upload_certificate.pem`.
3. Play Console → Test and release → App integrity → **Upload key certificate**:
   check that its SHA-256 matches `keystore-fingerprint.md` exactly.

- Evidence: upload-key SHA-256 matches local fingerprint, checked `YYYY-MM-DD`
  (fingerprint: `AB:CD:…`)

## 5. First internal-testing release (manual)

```sh
op run --env-file=release.env -- ./gradlew bundleRelease   # or plain ./gradlew bundleRelease with keystore.properties
scripts/verify-release-signature.sh         # must pass before upload
```

The privacy policy and the data-safety answers say the app has no internet
access. Check that the merged manifest has no `INTERNET` permission:

```sh
grep -c 'android.permission.INTERNET' \
  app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml
# expected: 0 (the exact path can vary between AGP versions)
```

The expected version is `versionCode` 1 / `versionName` "1.0".

Play Console → Testing → **Internal testing** → Create release → upload
`app/build/outputs/bundle/release/app-release.aab`. Add short Swedish release
notes, for example `Första testversionen.`

- Evidence: release `1 (1.0)` accepted on internal track `YYYY-MM-DD`

## 6. Service account for automated uploads

1. Play Console → Setup → **API access** → link or create a Google Cloud project.
2. In Google Cloud: enable the **Google Play Android Developer API**, then create
   a service account (for example `play-publisher`) and a **JSON key**.
3. Play Console → Users and permissions → invite the service-account email
   with **Release manager** only, for app `se.simmarken`.
4. GitHub → repository Settings → Secrets and variables → Actions → new
   secret **`PLAY_SERVICE_ACCOUNT_JSON`** containing the full JSON key.
5. Delete the local JSON file (or store it in a password manager). It must never
   be in the repository. `.gitignore` covers `**/service-account*.json`.

- Evidence: secret created `YYYY-MM-DD`; service-account email `…@….iam.gserviceaccount.com`

## 7. Internal testers

Internal testing → **Testers** → create an email list with the maintainer's
address only. Copy the opt-in link and install the app from Play on a real
device.

To add family testers later, add their Google account emails to the same list
and send them the opt-in link. Internal testing allows up to 100 testers and
needs no review.

- Evidence: installed from Play on `<device>` `YYYY-MM-DD`

---

When all steps are done, tick RELE-01, RELE-02 and RELE-05 in
[PRD-0002](prd/0002-release-and-open-source.md), linking to this file. The
release pipeline (CI-02, RELE-03, RELE-04) is next.
