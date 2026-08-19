package dev.foss.goldenpath.ui.theme

enum class ThemeMode {
    System,
    Light,
    Dark,
}

fun ThemeMode.next(): ThemeMode = when (this) {
    ThemeMode.System -> ThemeMode.Light
    ThemeMode.Light -> ThemeMode.Dark
    ThemeMode.Dark -> ThemeMode.System
}
