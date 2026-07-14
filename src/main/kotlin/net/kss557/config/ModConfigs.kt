package net.kss557.config

import com.mojang.datafixers.util.Pair
import net.kss557.InfinityVaults

object ModConfigs {

    lateinit var CONFIG: SimpleConfig
        private set

    private lateinit var configs: ModConfigProvider

    var VAULT_COOLDOWN: Long = 0
    var DROP_DISPLAYED_ITEM: Boolean = true

    enum class ConfigType { LONG, BOOLEAN }

    data class ConfigEntry(
        val key: String,
        val type: ConfigType,
        val defaultValue: Any,
        val description: String,
        val get: () -> Any,
        val set: (Any) -> Unit
    )

    val configEntries = LinkedHashMap<String, ConfigEntry>()

    fun registerConfigs() {
        configs = ModConfigProvider()
        createConfigs()
        CONFIG = SimpleConfig.of(InfinityVaults.MOD_ID + "config").provider(configs).request()
        assignConfigs()
        registerEntries()
    }

    private fun createConfigs() {
        configs.addKeyValuePair(Pair.of("vault.cooldown", 36000L), "Vault reload in ticks, 36000 tick = 30 minutes. -1 disables cooldown")
        configs.addKeyValuePair(Pair.of("drop.displayed.item", true), "If set to true, the first item to drop when the vault is opened will be the one that was displayed when the vault was opened. false will work just like in vanilla Minecraft")
    }

    private fun assignConfigs() {
        VAULT_COOLDOWN = CONFIG.getOrDefault("vault.cooldown", 36000L)
        DROP_DISPLAYED_ITEM = CONFIG.getOrDefault("drop.displayed.item", true)
        println("All ${configs.getConfigsList().size} have been set properly")
    }

    private fun registerEntries() {
        configEntries["vault.cooldown"] = ConfigEntry(
            key = "vault.cooldown",
            type = ConfigType.LONG,
            defaultValue = 36000L,
            description = "Vault reload in ticks. -1 disables cooldown",
            get = { VAULT_COOLDOWN },
            set = { value ->
                VAULT_COOLDOWN = value as Long
                CONFIG.set("vault.cooldown", value.toString())
                CONFIG.save()
            }
        )

        configEntries["drop.displayed.item"] = ConfigEntry(
            key = "drop.displayed.item",
            type = ConfigType.BOOLEAN,
            defaultValue = true,
            description = "Drop the displayed item first when vault is opened",
            get = { DROP_DISPLAYED_ITEM },
            set = { value ->
                DROP_DISPLAYED_ITEM = value as Boolean
                CONFIG.set("drop.displayed.item", value.toString())
                CONFIG.save()
            }
        )
    }
}
