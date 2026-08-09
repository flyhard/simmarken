# Phase 10: Play Console Setup - Context

**Gathered:** 2026-08-09
**Status:** Ready for planning

<domain>
## Phase Boundary

Manual Play Store bootstrap for `se.simmarken`: create the Play Console app record, enroll Play App Signing with the Phase 8 upload keystore, configure a GCP service account with Release Manager access, store `PLAY_SERVICE_ACCOUNT_JSON` in GitHub Secrets, and get at least one locally signed AAB accepted on the **internal testing** track via a one-time manual Console upload. Deliverables include a committed maintainer runbook (`docs/PLAY-CONSOLE-SETUP.md`) with evidence slots for phase verification. Does not include GPP Gradle plugin wiring, release CI workflow, or automated uploads (Phase 11), keystore GitHub Secrets (Phase 11), or open-source publishing (Phase 12).

</domain>

<decisions>
## Implementation Decisions

### First AAB Upload (Bootstrap)
- **D-01:** Manual Console upload — Upload the first signed AAB via Play Console UI; Play Developer API cannot register a new app. GPP/API uploads begin in Phase 11 after the app record exists.
- **D-02:** Bootstrap version — Use existing Gradle defaults: `versionCode = 1`, `versionName = "1.0"`.
- **D-03:** Bootstrap artifact source — Build locally: `./gradlew :app:bundleRelease` with `keystore.properties`, then validate with `scripts/verify-release-signature.sh` before uploading `app/build/outputs/bundle/release/app-release.aab`.
- **D-04:** Bootstrap evidence — Commit `docs/PLAY-CONSOLE-SETUP.md` with checklist steps and evidence slots (screenshot URLs, release IDs, acceptance confirmation) for phase verification and Phase 11 handoff.

### Service Account & GitHub Secrets
- **D-05:** Play Console permission — Grant the service account **Release Manager** only (least privilege for internal-track uploads).
- **D-06:** GitHub Secret name — `PLAY_SERVICE_ACCOUNT_JSON` (full service account JSON key). — **Reversibility:** costly — Phase 11 GPP workflow and docs will reference this exact name.
- **D-07:** GCP project — Create the service account in the **Play Console linked GCP project** (Setup → API access), enable Play Developer API, then invite the SA under Users and permissions.
- **D-08:** Phase 10 secret scope — Store only `PLAY_SERVICE_ACCOUNT_JSON` in GitHub Secrets during Phase 10. Keystore secrets (`ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are Phase 11.

### Internal Testing Scope
- **D-09:** Testing track — **Internal testing** track only (matches ROADMAP/REQUIREMENTS; GPP `track = "internal"` in Phase 11).
- **D-10:** Tester list — Maintainer only for v1.1 bootstrap; document how to add family testers later.
- **D-11:** Release notes — Swedish, minimal (e.g. "Första interna testversion") for the bootstrap internal release.
- **D-12:** Track identification — Use Play's default internal testing track; no custom track name.

### Store Listing Minimum
- **D-13:** App display name — **Simmärken** in Play Console.
- **D-14:** Default store locale — Swedish (Sweden) `sv-SE`.
- **D-15:** Privacy policy — Simple hosted policy (GitHub Pages or equivalent) stating offline-only operation, no data collection, local Room storage, optional JSON export; link URL entered in Play Console. — **Reversibility:** one-way — Play Console stores the policy URL; changing hosting later requires Console update.
- **D-16:** Content rating — Complete the IARC questionnaire honestly (offline parent app, no ads, no data collection, no IAP); expect Everyone/PEGI 3 equivalent.

### Claude's Discretion
- Exact GPP setup step ordering and Console navigation labels in the runbook (follow `.planning/research/PITFALLS.md` GPP order).
- GitHub Pages vs gist URL for privacy policy hosting (must be a stable HTTPS URL for Play Console).
- Evidence field format in `docs/PLAY-CONSOLE-SETUP.md` (screenshot paths, Console release IDs, dates).
- Whether to add a `docs/PRIVACY.md` source in repo that mirrors the hosted policy (optional; hosted URL is the Play requirement).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements & Roadmap
- `.planning/REQUIREMENTS.md` — RELE-01 (Play App Signing enrollment), RELE-02 (app record `se.simmarken`); out-of-scope: production auto-deploy, Fastlane, committing credentials
- `.planning/ROADMAP.md` — Phase 10 goal and four success criteria; depends on Phase 8
- `.planning/PROJECT.md` — v1.1 infrastructure-only; internal testing track; Gradle Play Publisher 4.0.0 (no Fastlane)
- `.planning/STATE.md` — Current milestone position

### Prior Phase Context
- `.planning/phases/08-gradle-signing-configuration/08-CONTEXT.md` — D-16 fingerprint capture in Phase 8, Play registration in Phase 10; D-12 CI env-var contract for keystore (Phase 11)
- `.planning/phases/08-gradle-signing-configuration/08-VERIFICATION.md` — Play App Signing enrollment deferred to Phase 10
- `.planning/phases/09-ci-build-test/09-CONTEXT.md` — CI workflow separate from release; debug lint/test unaffected by signing

### Signing & Verification (Phase 8 outputs)
- `app/build.gradle.kts` — `applicationId = "se.simmarken"`, `versionCode = 1`, `versionName = "1.0"`
- `scripts/generate-upload-keystore.sh` — Upload keystore bootstrap; writes gitignored `keystore-fingerprint.md`
- `scripts/verify-release-signature.sh` — Post-build AAB signature validation
- `keystore.properties.example` — Local signing template
- `docs/SECURITY-CHECKLIST.md` — Export JSON PII scope; signing file gitignore evidence

### Release Engineering Research
- `.planning/research/PITFALLS.md` — GPP setup order, upload-key mismatch pitfalls, manual-first-upload guidance
- `.planning/research/STACK.md` — Gradle Play Publisher 4.0.0, internal track default, first manual upload requirement
- `.planning/research/ARCHITECTURE.md` — `PLAY_SERVICE_ACCOUNT_JSON` secret name; Play App Signing architecture

### Phase 10 Deliverable (to be created)
- `docs/PLAY-CONSOLE-SETUP.md` — Maintainer runbook with evidence slots (D-04)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `scripts/verify-release-signature.sh`: Validates bootstrap AAB before manual upload; confirms SHA-256 matches gitignored `keystore-fingerprint.md`.
- `scripts/generate-upload-keystore.sh`: Already created `upload.jks`; Phase 10 registers its SHA-256 in Play Console App Signing.
- Phase 8 signed AAB path: `app/build/outputs/bundle/release/app-release.aab`.

### Established Patterns
- Solo-maintainer workflow — manual Console steps with committed runbook + evidence, not ad-hoc notes.
- Secrets never in source — service account JSON only in GitHub Secrets; keystore secrets deferred to Phase 11.
- Internal testing only — no production track exposure in v1.1.
- Gradle Play Publisher 4.0.0 chosen over Fastlane — Phase 10 sets up credentials GPP will consume in Phase 11.

### Integration Points
- Play Console → App integrity → Upload key certificate: must match Phase 8 `keystore-fingerprint.md` SHA-256.
- Play Console → Setup → API access → GCP linked project: service account creation target.
- GitHub repo Secrets → `PLAY_SERVICE_ACCOUNT_JSON`: consumed by Phase 11 GPP `publishBundle`.
- `docs/PLAY-CONSOLE-SETUP.md`: bridges manual Phase 10 work to automated Phase 11 pipeline.

</code_context>

<specifics>
## Specific Ideas

- User wants a committed checklist (`docs/PLAY-CONSOLE-SETUP.md`) with evidence slots — not planning-only notes.
- Bootstrap upload uses the already-validated local signing path, not CI.
- Store presence should feel Swedish-first: app name "Simmärken", default locale sv-SE, Swedish release notes.
- Privacy policy via simple GitHub Pages page — honest offline/no-collection statement aligned with SECURITY-CHECKLIST export PII scope.

</specifics>

<deferred>
## Deferred Ideas

- Family/co-parent testers on internal track — add after maintainer-only bootstrap validation.
- Keystore GitHub Secrets (`ANDROID_KEYSTORE_BASE64`, etc.) — Phase 11 release workflow.
- GPP Gradle plugin and `publishBundle` automation — Phase 11.
- `docs/PRIVACY.md` in repo as policy source — optional; hosted URL is the Play requirement.

</deferred>

---

*Phase: 10-Play Console Setup*
*Context gathered: 2026-08-09*
