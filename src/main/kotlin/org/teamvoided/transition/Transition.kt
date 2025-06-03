package org.teamvoided.transition

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.fzzyhmstrs.fzzy_config.api.ConfigApi.registerAndLoadConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.transition.config.MappingModes
import org.teamvoided.transition.config.TransitionConfig
import org.teamvoided.transition.mappings.MappingsManager

@Suppress("unused")
object Transition : ModInitializer {
    const val MODID = "transition"
    const val MINECRAFT = "minecraft"

    val LOGGER: Logger = LoggerFactory.getLogger(MODID)
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    @JvmField
    var IS_ACTIVE = false

    @JvmField
    val CONFIG: TransitionConfig = registerAndLoadConfig(::TransitionConfig)

    override fun onInitialize() {
        log("Transitioning Transition!")
        loadMod()
        ServerLifecycleEvents.SERVER_STARTING.register(::onServerStarting)
    }

    fun onServerStarting(server: MinecraftServer) {
        if (CONFIG.mode == MappingModes.ON_LOAD) {
            val worldFile = server.getWorldPath(LevelResource.ROOT).toFile()
            log("Server world world file: $worldFile")
            ServerProcessor.processDirectory(worldFile)
            log("Finished processing world file!")
            server.playerList.players.forEach { it.connection.disconnect(Component.literal("Transition end of world!")) }
        }
    }

    fun loadMod() {
        if (CONFIG.mode == MappingModes.OFF) return
//        ModManager.readCache()
        FabricLoader.getInstance().allMods.forEach {
            val data = it.metadata
            if (MappingsManager.loadModMappings(it, data.id, data.containsCustomValue("has_remapping"))) {
                ModManager.addActiveMod(data)
            }
        }
//        ModManager.writeCache()
    }

    @JvmStatic
    fun log(message: String?) = LOGGER.info("(Transition) {}", message)
    fun error(message: String?) = LOGGER.error("(Transition) {}", message)
    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)
    fun isDev() = FabricLoader.getInstance().isDevelopmentEnvironment
}
