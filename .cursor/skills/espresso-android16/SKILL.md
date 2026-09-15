---
name: espresso-android16
description: Pin Espresso 3.7+ for Android 16 Compose instrumented tests. Use when InputManager.getInstance fails on API 36.
disable-model-invocation: false
---

# Espresso on Android 16 (API 36)

See **KB-022** in `KNOWLEDGE_BASE.md`.

```bash
python3 scripts/agent-run.py check-espresso-android16
# or via feature-gate stage android-espresso-16
```

Pin in `examples/android` Gradle:

```kotlin
androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
```

Do not drop the pin for an unofficial “test BOM”. Nav Back smoke: home → settings → about → feedback; use Espresso `pressBack()` where Compose Back is under test.
