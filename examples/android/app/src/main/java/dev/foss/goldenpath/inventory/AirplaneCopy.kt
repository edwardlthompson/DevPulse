package dev.foss.goldenpath.inventory

object AirplaneCopy {
    fun tagged(line: String, airplane: Boolean): String =
        if (!airplane || line.isBlank()) line else "$line · airplane"
}
