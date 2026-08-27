package dev.foss.goldenpath.ui.settings

import android.content.Context
import android.widget.Toast

object SettingsToast {
    fun ok(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_LONG).show()
    }

    fun fail(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }
}
