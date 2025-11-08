package io.github.aidenk.sevenshuffle

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * CrashLogger
 *
 * A tiny, offline crash logger that writes uncaught exception reports to
 * /data/data/<pkg>/files/crashlogs on the device. Designed to run as early
 * as possible from Application.onCreate().
 *
 * Usage:
 *   class MyApp : Application() {
 *     override fun onCreate() {
 *       super.onCreate()
 *       CrashLogger.init(this)
 *     }
 *   }
 */
// CrashLogger.kt
object CrashLogger {
    private const val DIR = "logs"
    private lateinit var app: Application
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun init(application: Application) {
        app = application
        File(app.filesDir, DIR).mkdirs()
        writeHeaderOnce()

        // Keep the chain so we still get system crash dialogs & Play Console reports in debug/release
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 2) auto-log uncaught exceptions
            log(
                level = 'E',
                tag = "Uncaught",
                msg = "FATAL EXCEPTION in ${thread.name}",
                tr = throwable
            )
            // (optional) prompt user to export/share here
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    // --- Public helpers you can call from anywhere for breadcrumbs/non-fatals ---
    fun d(tag: String, msg: String) = log('D', tag, msg, null)
    fun i(tag: String, msg: String) = log('I', tag, msg, null)
    fun w(tag: String, msg: String, tr: Throwable? = null) = log('W', tag, msg, tr)
    fun e(tag: String, msg: String, tr: Throwable? = null) = log('E', tag, msg, tr)

    // --- Core writer: formats like Logcat and rotates files ---
    private fun log(level: Char, tag: String, msg: String, tr: Throwable?) {
        runCatching {
            val now = System.currentTimeMillis()
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(now))
            val pid = android.os.Process.myPid()
            val tid = android.os.Process.myTid()
            val line = "$ts $pid-$tid/$tag $level: $msg"
            val stack = tr?.let { "\n${Log.getStackTraceString(it)}" }.orEmpty()

            val file = currentFile()
            file.appendText(line + stack + "\n")
            rotateIfNeeded()
        }
    }

    private fun currentFile(): File =
        File(File(app.filesDir, DIR), "crash_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.log")
            .apply { if (!exists()) createNewFile() }

    private fun rotateIfNeeded(maxBytes: Long = 512 * 1024, keep: Int = 5) {
        val d = File(app.filesDir, DIR)
        val f = currentFile()
        if (f.length() > maxBytes) f.renameTo(File(d, "crash_${System.currentTimeMillis()}.log"))
        d.listFiles()?.sortedByDescending { it.lastModified() }?.drop(keep)?.forEach { it.delete() }
    }

    private fun writeHeaderOnce() {
        val f = currentFile()
        if (f.length() == 0L) {
            val pm = app.packageManager
            val pInfo = pm.getPackageInfo(app.packageName, 0)
            val header = buildString {
                appendLine("=== Seven Shuffle Crash Log ===")
                appendLine("App: ${app.packageName} v${pInfo.versionName} (${pInfo.longVersionCode})")
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Locale: ${Locale.getDefault()}  Timezone: ${TimeZone.getDefault().id}")
                appendLine("Started: ${Date()}")
                appendLine("================================")
            }
            f.appendText(header + "\n")
        }
    }

    // Optional: expose for your log viewer/export UI
    fun listLogs(): List<File> =
        File(app.filesDir, DIR).listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    fun read(file: File): String = file.readText()
}