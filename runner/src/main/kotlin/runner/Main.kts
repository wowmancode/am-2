package runner

import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import java.io.File
import kotlin.system.exitProcess

/**
 * Minimal headless patcher: loads a Morphe .mpp bundle and applies every
 * compatible patch to a stock APK, then writes the patched APK.
 *
 * Usage:
 *   runner --patches patches.mpp --input stock.apk --output patched.apk --cache /tmp/mcache
 *
 * ─────────────────────────────────────────────────────────────────────
 * VERIFY THIS: the app.morphe.patcher API surface used below
 * (Patcher / PatcherConfig / acceptPatches / applyPatches) matches
 * morphe-patcher 1.9.x as best as I can reconstruct it, but I could not
 * inspect the 1.9.0 JAR in this chat. If compilation fails on the
 * `Patcher(...)` call, mirror exactly how MorpheApp/morphe-cli invokes
 * the patcher — the entry point is unchanged from ReVanced patcher, and
 * it should be a <10-line fix. Everything around it (arg parsing,
 * working dir, error handling) stays as-is.
 * ─────────────────────────────────────────────────────────────────────
 */
fun main(args: Array<String>) {
    val opts = try {
        parseArgs(args)
    } catch (t: Throwable) {
        System.err.println("runner: ${t.message}")
        printUsage()
        exitProcess(2)
    }

    val patchesFile = File(opts.patches).also {
        require(it.isFile) { "patches bundle not found: ${it.absolutePath}" }
    }
    val inputFile = File(opts.input).also {
        require(it.isFile) { "input APK not found: ${it.absolutePath}" }
    }
    val outputFile = File(opts.output)
    val cacheDir = File(opts.cache).also { it.mkdirs() }

    outputFile.parentFile?.mkdirs()

    println("runner: patches = ${patchesFile.absolutePath}")
    println("runner: input   = ${inputFile.absolutePath}")
    println("runner: output  = ${outputFile.absolutePath}")
    println("runner: cache   = ${cacheDir.absolutePath}")

    val config = PatcherConfig(
        patchBundle = patchesFile,
        inputFile = inputFile,
        outputFile = outputFile,
        cacheDir = cacheDir,
    )

    val patcher = Patcher(config)

    val accepted = patcher.acceptPatches()
    println("runner: accepted patches → $accepted")

    val result = patcher.applyPatches()
    println("runner: applied patches → $result")

    require(outputFile.isFile) {
        "patcher returned without writing ${outputFile.absolutePath}"
    }
    println("runner: wrote ${outputFile.length()} bytes to ${outputFile.absolutePath}")
}

private data class Options(
    val patches: String,
    val input: String,
    val output: String,
    val cache: String,
)

private fun parseArgs(args: Array<String>): Options {
    var patches: String? = null
    var input: String? = null
    var output: String? = null
    var cache = "build/patcher-cache"

    var i = 0
    while (i < args.size) {
        when (val a = args[i]) {
            "--patches" -> patches = args.getOrNull(++i)
            "--input"   -> input   = args.getOrNull(++i)
            "--output"  -> output  = args.getOrNull(++i)
            "--cache"   -> cache   = args.getOrNull(++i) ?: cache
            else -> error("unknown argument: $a")
        }
        i++
    }

    return Options(
        patches = patches ?: error("--patches is required"),
        input = input ?: error("--input is required"),
        output = output ?: error("--output is required"),
        cache = cache,
    )
}

private fun printUsage() {
    System.err.println(
        """
        usage: runner --patches <patches.mpp> --input <stock.apk> --output <patched.apk> [--cache <dir>]
        """.trimIndent()
    )
}
