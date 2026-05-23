package net.kss557

import net.fabricmc.api.ModInitializer
import net.kss557.command.CommandRegistries
import net.kss557.config.ModConfigs
import org.slf4j.LoggerFactory

object InfinityVaults : ModInitializer {
	val MOD_ID: String = "infinityvaults"
	val LOGGER = LoggerFactory.getLogger("infinityvaults")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!")
		ModConfigs.registerConfigs()
		CommandRegistries.registerCommands()
	}
}