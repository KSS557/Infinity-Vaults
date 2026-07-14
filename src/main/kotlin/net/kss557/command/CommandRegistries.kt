package net.kss557.command

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.kss557.command.getItem.GetItemCommand

object CommandRegistries {

    fun registerCommands() {
        CommandRegistrationCallback.EVENT.register(ClearVaultsCommand::register)
        CommandRegistrationCallback.EVENT.register(ReplaceVaultsCommand::register)
        CommandRegistrationCallback.EVENT.register(GetItemCommand::register)
        CommandRegistrationCallback.EVENT.register(ConfigCommand::register)
    }

}