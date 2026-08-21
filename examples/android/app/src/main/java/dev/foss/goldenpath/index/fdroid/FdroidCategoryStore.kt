package dev.foss.goldenpath.index.fdroid

import java.io.File

data class FdroidPackageMeta(
    val category: String?,
    val related: List<String> = emptyList(),
)

interface FdroidCategoryStore {
    fun load(): Map<String, FdroidPackageMeta>
    fun save(all: Map<String, FdroidPackageMeta>)
    fun putAll(records: List<FdroidAppRecord>)
}

object FdroidCategoryCodec {
    fun encode(all: Map<String, FdroidPackageMeta>): String = buildString {
        all.forEach { (pkg, meta) ->
            if (pkg.isBlank()) return@forEach
            val category = meta.category?.trim().orEmpty()
            val related = meta.related.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(",")
            if (category.isEmpty() && related.isEmpty()) return@forEach
            append(esc(pkg)).append('\t').append(esc(category)).append('\t').append(esc(related)).append('\n')
        }
    }

    fun decode(raw: String): Map<String, FdroidPackageMeta> {
        if (raw.isBlank()) return emptyMap()
        val out = linkedMapOf<String, FdroidPackageMeta>()
        raw.lineSequence().forEach { line ->
            val parts = line.split('\t')
            if (parts.isEmpty()) return@forEach
            val pkg = unesc(parts[0])
            if (pkg.isEmpty()) return@forEach
            val category = parts.getOrNull(1)?.let(::unesc)?.ifEmpty { null }
            val related = parts.getOrNull(2)?.let(::unesc)?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                .orEmpty()
            if (category == null && related.isEmpty()) return@forEach
            out[pkg] = FdroidPackageMeta(category, related)
        }
        return out
    }

    fun fromRecords(records: List<FdroidAppRecord>): Map<String, FdroidPackageMeta> =
        records.mapNotNull { rec ->
            val category = rec.category?.trim()?.ifEmpty { null }
            val related = rec.relatedPackages.map { it.trim() }.filter { it.isNotEmpty() }
            if (category == null && related.isEmpty()) null else rec.packageName to FdroidPackageMeta(category, related)
        }.toMap()

    private fun esc(value: String): String = value.replace("\t", " ").replace("\n", " ")
    private fun unesc(value: String): String = value.trim()
}

class FileFdroidCategoryStore(private val file: File) : FdroidCategoryStore {
    private val lock = Any()

    override fun load(): Map<String, FdroidPackageMeta> = synchronized(lock) { read() }

    override fun save(all: Map<String, FdroidPackageMeta>) {
        synchronized(lock) { write(all) }
    }

    override fun putAll(records: List<FdroidAppRecord>) {
        val extra = FdroidCategoryCodec.fromRecords(records)
        if (extra.isEmpty()) return
        synchronized(lock) { write(read() + extra) }
    }

    private fun read(): Map<String, FdroidPackageMeta> =
        runCatching { FdroidCategoryCodec.decode(file.readText(Charsets.UTF_8)) }.getOrDefault(emptyMap())

    private fun write(all: Map<String, FdroidPackageMeta>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(FdroidCategoryCodec.encode(all), Charsets.UTF_8)
        }
    }
}
