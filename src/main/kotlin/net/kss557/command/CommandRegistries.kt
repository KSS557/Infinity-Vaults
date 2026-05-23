package net.kss557.command

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object CommandRegistries {

    fun registerCommands() {
        CommandRegistrationCallback.EVENT.register(ClearVaultsCommand::register)
    }

}