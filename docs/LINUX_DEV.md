# Linux developer optimizations

> Maximize a Linux laptop/desktop for this template before paying for Cloud Agents.
> Companion: [`.cursor/rules/local-compute.mdc`](../.cursor/rules/local-compute.mdc), [`LOCAL_MODELS.md`](LOCAL_MODELS.md), [`PARALLEL_AGENT_SCOPES.md`](PARALLEL_AGENT_SCOPES.md).

Probe the machine anytime:

```bash
python3 scripts/agent-run.py check-local-compute
# or
just local-compute

```

## Defaults for this template

```bash
# ~/.bashrc, ~/.zshrc, or direnv (.envrc from .envrc.example)
export BOOTSTRAP_CHECK_JOBS="$(nproc)"   # or lower if RAM is tight
export FEATURE_GATE_JOBS=2               # use 1 on ≤16 GB RAM
# Optional: FEATURE_GATE_TIMEOUT=600
python3 scripts/agent-run.py verify

```

Copy [`.envrc.example`](../.envrc.example) → `.envrc` and run `direnv allow` if you use [direnv](https://direnv.net/).

## Filesystem and disk

| Do | Avoid |
|----|--------|
| Keep clones on local **ext4** / **XFS** SSDs | Network home mounts / FUSE for hot repos |
| Share caches: `~/.gradle`, `~/.npm`, `~/.cache/uv`, Cargo/`go` caches | Re-downloading SDKs inside every worktree |
| Put scratch builds on **tmpfs** when RAM allows (`export TMPDIR=/dev/shm/...`) | Letting desktop search index `node_modules`, `.gradle`, `target`, `dist` |
Exclude heavy dirs from Trackers/Recoll/ baloo if they scan your tree.

## CPU scheduling

- Prefer stack-native parallelism already wired here: pytest-xdist `-n auto`, Vitest `maxWorkers: 50%`, Gradle `--parallel`.
- Cap with `BOOTSTRAP_CHECK_JOBS` / `FEATURE_GATE_JOBS` instead of guessing.
- For long foreground builds that must not starve the IDE: `nice -n 10 ionice -c2 -n7 ./gradlew …`.
- Instrumented Android tests (AVD + host I/O heavy) — wrap the connected suite the same way so Cursor stays responsive:

```bash
cd examples/android
nice -n 10 ionice -c2 -n7 ./gradlew connectedDebugAndroidTest --no-daemon

```

  Or via the agent entrypoint when an emulator is already up: `nice -n 10 ionice -c2 -n7 python3 scripts/agent-run.py emulator` (see `/emulator`). Skip when `ANDROID_EMULATOR_LOCAL=0`.
- Flaky parallel gates → `FEATURE_GATE_JOBS=1` (documented escape hatch), not “turn off tests”.

## Tooling install once

Install host tools once (distro packages, [uv](https://docs.astral.sh/uv/), [mise](https://mise.jdx.dev/), or [asdf](https://asdf-vm.com/)):

- Python 3.11+, Node 22+, JDK 17+, Android cmdline-tools when on the Android stack
- Optional: `just`, `pre-commit`, `shellcheck`, `actionlint`, [Nix](https://nixos.org) (`nix run .#verify` wraps `scripts/verify.sh`; CI job is path-filtered)

Worktrees should **reuse** those tools and user-level caches. Prefer fail-soft `.cursor/setup-worktree-unix.sh` (see [`PARALLEL_AGENT_SCOPES.md`](PARALLEL_AGENT_SCOPES.md)) over copying entire SDKs per worktree.

### Apt-locked hosts (user-local JDK + Android SDK)

If the distro blocks `apt install openjdk` / SDK packages, install once under `$HOME` and point the shell (or direnv) at them:

- JDK: micromamba/`conda` env under `~/.local/micromamba/envs/tools` (or any JDK 17+ tree)
- SDK: `~/Android/Sdk` or `~/.local/android` with `platform-tools`
- Symlink `adb`/`git`/`gh` into `~/.local/bin` if needed

`python3 scripts/agent-run.py …` prepends those PATH prefixes via `scripts/lib/agent_run_env.py`. Copy [`.envrc.example`](../.envrc.example) for `JAVA_HOME` / `ANDROID_HOME` exports.

When init **prunes** unused stacks, you do not need `npm`/`uv` for those trees — `feature-gate` skips missing toolchains once with an install HINT. Install Node/uv only for stacks you keep.

## Shell ergonomics

| Tool | Role |
|------|------|
| **direnv** | Per-repo env from `.envrc` (never commit secrets; `.env` stays gitignored) |
| **zoxide** / **fzf** | Fast directory and history jump |
| **just** | Human/agent shared recipes (`just verify`, `just local-compute`) |
| **git worktree** | Isolated checkouts for `/worktree` and `/best-of-n` |
Keep one entrypoint (`just` / `scripts/verify.sh`) so agents and humans run the same gates.

## Containers

- Day-to-day edit/test loops: **native Linux** is usually faster than Docker-in-Docker.
- Use containers for clean distro parity, release packaging, or “no host JDK” CI mirrors — not for every `vitest` run.
- Prefer sharing host Gradle/npm caches into containers when you do use them.

## File watchers (inotify)

Large monorepos can exhaust default watches (Vite, Gradle, IDEs drop events).

Check:

```bash
cat /proc/sys/fs/inotify/max_user_watches

```

If you see watcher errors, raise temporarily:

```bash
sudo sysctl -w fs.inotify.max_user_watches=524288

```

Persist under `/etc/sysctl.d/` (e.g. `60-inotify.conf`) when it helps your machine. `check-local-compute` prints a Linux hint when the limit looks low.

## Laptop vs CI

| Run locally | Leave for CI |
|-------------|--------------|
| Lint, unit tests, `verify`, `/update-deps` patch/minor | Android emulator jobs, CodeQL, multi-OS upgrade sim |
| `/best-of-n` on flaky gate fixes | Release Please / scorecard / required branch checks |
Do not wait on Dependabot for patch/minor — run `/update-deps` first.

## Parallel agents and worktrees

On This Computer:

1. Sequential BUILD_PLAN lock, then `/scope` with `agent_count >= 2`
2. Non-overlapping Parallel rows in one message (concurrent Task subagents)
3. `/worktree` or `scripts/setup-agent-worktrees.sh` for heavy installs
4. `/best-of-n` to race models on hard fixes (never push from workers)

Details: [`PARALLEL_AGENT_SCOPES.md`](PARALLEL_AGENT_SCOPES.md).

## Android on Linux (KVM, udev, plugdev)

- **KVM:** `/dev/kvm` must exist and be readable for the AOSP emulator (`check-local-compute` prints `kvm=yes|no`). Without KVM, use a USB device (`ANDROID_SERIAL=…`) — do not expect `/emulator` to start.
- **udev / plugdev:** After plugging a phone, `adb devices` may show `unauthorized` or missing until the user accepts the RSA dialog **and** the USB device node is writable.

Example udev rule (adjust `idVendor` from `lsusb`):

```bash
# /etc/udev/rules.d/51-android.rules
SUBSYSTEM=="usb", ATTR{idVendor}=="2a70", MODE="0666", GROUP="plugdev"
# then: sudo udevadm control --reload-rules && sudo udevadm trigger
# add yourself: sudo usermod -aG plugdev "$USER"  (re-login)

```

If `adb` still needs `sudo chmod` on `/dev/bus/usb/...`, the rule did not match — fix vendor/product ids rather than chmod every session.

## Optional local models and GPU

- Ollama / LM Studio on `127.0.0.1` only — [`LOCAL_MODELS.md`](LOCAL_MODELS.md)
- Template gates do not require CUDA; `/emulator` may use `-gpu host` when KVM is available (`kvm=yes` from `check-local-compute`)
- Optional Blender icon factory: Cycles CPU in CI; `BLENDER_CYCLES_DEVICE=OPTIX` only on This Computer (`modules/blender/MODULE.md`)

## Quick checklist

- [ ] Repo on local SSD (ext4/XFS)
- [ ] `direnv allow` or shell exports for `BOOTSTRAP_CHECK_JOBS` / `FEATURE_GATE_JOBS`
- [ ] Shared package caches in `$HOME`
- [ ] inotify watches high enough for Vite/IDE
- [ ] `just local-compute` shows sane `jobs=` / `slots=` / `kvm=`
- [ ] Prefer worktrees + local gates over Cloud Agents when the machine is free

## Appendix — OnePlus / OEM USB quirks

| Symptom | Likely cause | Mitigations |
|---------|--------------|-------------|
| `adb devices` unauthorized forever | Vendor RSA dialog missed / wrong adbkey | Revoke USB debugging authorizations; delete stale `~/.android/adbkey*`; re-plug |
| Device lists then drops | OnePlus “USB charging only” / oxygen selective USB | Set USB mode to File transfer / MIDI; disable USB selective suspend for the controller |
| Multiple `adb` binaries | Distro `adb` vs SDK platform-tools | Prefer `$ANDROID_HOME/platform-tools` first on `PATH` (`check-local-compute`) |
| Wireless debug pairing fails | OEM mDNS / VPN | Use USB for instrumented smoke; pair on same LAN without VPN |
Never commit `ADB_VENDOR_KEYS`. Template CI does not require a physical OEM device.
