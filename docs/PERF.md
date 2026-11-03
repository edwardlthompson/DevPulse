# Performance — 200-app scan

Instrumented via `ScanPerf` (`examples/android/.../inventory/ScanPerf.kt`).
Cold start is marked in `MainActivity.onCreate`; duration and peak heap are sampled around `ReleaseRefreshRunner`.

## Measured (local harness, 200 apps)

JVM unit test `ScanPerf200Test` — F-Droid index parse + probe for 200 packages, no live network:

| Metric | Value |
|--------|------:|
| App count | 200 |
| Cold start | 420 ms |
| Total scan duration | 96 ms |
| Peak heap (JVM used) | 35.5 MiB (37 230 696 bytes) |

Command:

```bash
cd examples/android
./gradlew :app:testDebugUnitTest --tests "dev.foss.goldenpath.inventory.ScanPerf200Test"
```

Look for the `PERF_200` log line.

## Device network baseline (scaled)

On-device refresh log (388 apps, cached indexes + dump stores + leftover GitHub window) completed in **82.5 s**. Linear scale to 200 apps ≈ **42.5 s** wall time for a comparable full-network path. Peak RSS on device is not captured by the JVM harness; use `scan_perf` logcat lines from a real run for RSS.

## Budgets (targets)

| Metric | Target |
|--------|--------|
| Cold start to first frame | ≤ 1 500 ms |
| 200-app local index probe | ≤ 5 000 ms |
| 200-app full network refresh | ≤ 120 000 ms |
| Peak heap during scan | ≤ 256 MiB |
