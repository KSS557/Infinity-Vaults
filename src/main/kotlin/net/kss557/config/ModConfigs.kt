package net.kss557.config

import com.mojang.datafixers.util.Pair
import net.kss557.InfinityVaults

object ModConfigs {

    lateinit var CONFIG: SimpleConfig

    private lateinit var configs: ModConfigProvider

    var VAULT_COOLDOWN: Long = 0
    var DROP_DISPLAYED_ITEM: Boolean = true

    fun registerConfigs() {
        configs = ModConfigProvider()
        createConfigs()
        CONFIG = SimpleConfig.of(InfinityVaults.MOD_ID + "config").provider(configs).request()
        assignConfigs()
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
}