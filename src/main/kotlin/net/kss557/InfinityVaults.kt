package net.kss557

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.kss557.command.CommandRegistries
import net.kss557.command.getItem.VaultItemWatcher
import net.kss557.config.ModConfigs
import org.slf4j.LoggerFactory

object InfinityVaults : ModInitializer {
	val MOD_ID: String = "infinityvaults"
	val LOGGER = LoggerFactory.getLogger("infinityvaults")

	override fun onInitialize() {
		LOGGER.info("Hello Fabric world!")
		ModConfigs.registerConfigs()
		CommandRegistries.registerCommands()

		ServerTickEvents.END_SERVER_TICK.register { server ->
			VaultItemWatcher.onServerTick()
			VaultItemWatcher.tick(server)
		}
	}
}