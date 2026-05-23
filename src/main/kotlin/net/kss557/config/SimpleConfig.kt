package net.kss557.config

/*
 * Copyright (c) 2021 magistermaks
 * Slightly modified by Kaupenjoe 2021
 * Modified by KSS557 2026
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.Scanner

class SimpleConfig private constructor(
    private val request: ConfigRequest
) {

    companion object {
        private val LOGGER = LogManager.getLogger("SimpleConfig")

        fun of(filename: String): ConfigRequest {
            val path: Path = FabricLoader.getInstance().configDir
            return ConfigRequest(
                path.resolve("$filename.properties").toFile(),
                filename
            )
        }
    }

    private val config = HashMap<String, String>()

    var broken = false
        private set

    interface DefaultConfig {
        fun get(namespace: String): String

        companion object {
            fun empty(namespace: String): String {
                return ""
            }
        }
    }

    class ConfigRequest(
        val file: File,
        val filename: String
    ) {

        private var provider: DefaultConfig =
            object : DefaultConfig {
                override fun get(namespace: String): String {
                    return ""
                }
            }

        fun provider(provider: DefaultConfig): ConfigRequest {
            this.provider = provider
            return this
        }

        fun request(): SimpleConfig {
            return SimpleConfig(this)
        }

        fun getConfig(): String {
            return provider.get(filename) + "\n"
        }
    }

    init {
        val identifier = "Config '${request.filename}'"

        if (!request.file.exists()) {
            LOGGER.info("$identifier is missing, generating default one...")

            try {
                createConfig()
            } catch (e: Exception) {
                LOGGER.error("$identifier failed to generate!")
                LOGGER.error(e)
                broken = true
            }
        }

        if (!broken) {
            try {
                loadConfig()
            } catch (e: Exception) {
                LOGGER.error("$identifier failed to load!")
                LOGGER.error(e)
                broken = true
            }
        }
    }

    private fun createConfig() {
        request.file.parentFile.mkdirs()

        Files.createFile(request.file.toPath())

        PrintWriter(request.file, Charsets.UTF_8).use { writer ->
            writer.write(request.getConfig())
        }
    }

    private fun loadConfig() {
        Scanner(request.file).use { reader ->

            var line = 1

            while (reader.hasNextLine()) {
                parseConfigEntry(reader.nextLine(), line)
                line++
            }
        }
    }

    private fun parseConfigEntry(entry: String, line: Int) {

        if (entry.isNotEmpty() && !entry.startsWith("#")) {

            val parts = entry.split("=", limit = 2)

            if (parts.size == 2) {

                val value = parts[1]
                    .split(" #")[0]

                config[parts[0]] = value

            } else {
                throw RuntimeException(
                    "Syntax error in config file on line $line!"
                )
            }
        }
    }

    @Deprecated("")
    fun get(key: String): String? {
        return config[key]
    }

    fun getOrDefault(key: String, def: String): String {
        return get(key) ?: def
    }

    fun getOrDefault(key: String, def: Int): Int {
        return try {
            get(key)?.toInt() ?: def
        } catch (_: Exception) {
            def
        }
    }

    fun getOrDefault(key: String, def: Long): Long {
        return try {
            get(key)?.toLong() ?: def
        } catch (_: Exception) {
            def
        }
    }

    fun getOrDefault(key: String, def: Boolean): Boolean {

        val value = get(key)

        return value?.equals("true", ignoreCase = true)
            ?: def
    }

    fun getOrDefault(key: String, def: Double): Double {
        return try {
            get(key)?.toDouble() ?: def
        } catch (_: Exception) {
            def
        }
    }

    fun delete(): Boolean {

        LOGGER.warn(
            "Config '${request.filename}' was removed from existence! Restart the game to regenerate it."
        )

        return request.file.delete()
    }
}