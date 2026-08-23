package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FdroidApkFilesTest {
    @Test
    fun highestApkNameAndUrl() {
        val raw = """
            {"packages":{"org.ver":[
              {"versionName":"1.0","versionCode":1,"apkName":"org.ver_1.apk"},
              {"versionName":"3.2.1","versionCode":32,"apkName":"org.ver_32.apk"}
            ]}}
        """.trimIndent()
        assertEquals("org.ver_32.apk", FdroidApkFiles.namesIn(raw, setOf("org.ver"))["org.ver"]?.apkName)
        assertEquals("https://f-droid.org/repo/org.ver_32.apk", FdroidApkUrl.of("official", "org.ver_32.apk"))
        assertNull(FdroidApkUrl.of("official", "../evil.apk"))
    }

    @Test
    fun parserKeepsApkName() {
        val raw = """
            {"apps":[{"packageName":"org.ver","suggestedVersionName":"1.0","lastUpdated":1700000000000}],
             "packages":{"org.ver":[{"versionName":"1.0","versionCode":1,"apkName":"org.ver_1.apk"}]}}
        """.trimIndent()
        assertEquals("org.ver_1.apk", FdroidIndexParser.parse(raw, "official").single().apkName)
    }

    @Test
    fun highestKeepsSha256AndNativecode() {
        val raw = """
            {"packages":{"org.ver":[
              {"versionCode":1,"apkName":"org.ver_1.apk","hash":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
              {"versionCode":32,"apkName":"org.ver_32.apk","hash":"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb","nativecode":["arm64-v8a"],"size":4200000,"minSdkVersion":26}
            ]}}
        """.trimIndent()
        val hint = FdroidApkFiles.namesIn(raw, setOf("org.ver"))["org.ver"]
        assertEquals("org.ver_32.apk", hint?.apkName)
        assertEquals("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", hint?.sha256)
        assertEquals(setOf("arm64-v8a"), hint?.nativeCodes)
        assertEquals(4_200_000L, hint?.sizeBytes)
        assertEquals(26, hint?.minSdk)
        val rec = FdroidIndexParser.parse(
            """{"apps":[{"packageName":"org.ver","lastUpdated":1}],"packages":{"org.ver":[{"versionCode":32,"apkName":"org.ver_32.apk","hash":"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"}]}}""",
            "official",
        ).single()
        assertEquals("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", rec.apkSha256)
    }
}
