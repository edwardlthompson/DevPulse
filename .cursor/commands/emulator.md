# Local Android emulator (host GPU)

> Skills: `.cursor/skills/emulator/`

Run AOSP instrumented tests on this machine. Do **not** `git push`. Do **not** `adb reboot` or wipe a device this script did not start. `/ship` does not require this.

## Run

```bash
python3 scripts/agent-run.py run-android-emulator-local
```

- Missing SDK, hypervisor, or licenses → **SKIP exit 0** (not a failure).
- If adb already lists a `device`, tests run on that device and the script will not kill it.
- Otherwise the script may start AVD `goldenpath-api34` (`-gpu host`, then `swiftshader_indirect`). Pass `--keep-emulator` to leave it running.
- First image download needs `[ADB]` license accept (`sdkmanager --licenses`). The script never auto-accepts licenses.

`/gates` uses `--if-device` only (no silent image pull):

```bash
python3 scripts/agent-run.py run-android-emulator-local -- --if-device
```

Force skip: `ANDROID_EMULATOR_LOCAL=0`.

See `modules/android/MODULE.md` and `examples/android/README.md`. GitHub `android-instrumented` remains backup.

Begin now.
