package dev.foss.goldenpath.inventory

import java.io.File

internal data class UpdateAllFetched(
    val job: UpdateAllJob,
    val files: List<File>?,
    val why: InstallWhy = InstallWhy.NoFile,
)
