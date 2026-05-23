package net.kss557.config

import com.mojang.datafixers.util.Pair

class ModConfigProvider : SimpleConfig.DefaultConfig {

    private var configContents = ""

    private val configsList = mutableListOf<Pair<String, *>>()

    fun getConfigsList(): List<Pair<String, *>> {
        return configsList
    }

    fun addKeyValuePair(
        keyValuePair: Pair<String, *>,
        comment: String
    ) {

        configsList.add(keyValuePair)

        configContents += buildString {

            append(keyValuePair.first)
            append("=")
            append(keyValuePair.second)
            append(" #")
            append(comment)
            append(" | default: ")
            append(keyValuePair.second)
            append("\n")
        }
    }

    override fun get(namespace: String): String {
        return configContents
    }
}