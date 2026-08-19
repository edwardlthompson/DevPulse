package dev.foss.goldenpath

import android.content.Context
import java.io.File

/** Clears DataStore preference files so Robolectric tests do not share state. */
fun Context.clearPreferenceDataStores() {
    dataDir.resolve("datastore").listFiles()?.forEach { file: File ->
        file.delete()
    }
}
