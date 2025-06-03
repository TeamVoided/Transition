package org.teamvoided.transition.mappings

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.fabricmc.loader.api.ModContainer
import net.minecraft.util.GsonHelper
import org.teamvoided.transition.Transition
import org.teamvoided.transition.Transition.MODID
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object MappingsManager {
    fun loadModMappings(mod: ModContainer, modId: String, hasRemapping: Boolean): Boolean {
        val path = mod.findPath("data/$MODID/mappings.json")
        if (!path.isPresent) {
            if (hasRemapping) Transition.LOGGER.error("Failed to find mappings file for: {}", modId)
            return false
        }

        try {
            BufferedReader(
                InputStreamReader(path.get().toUri().toURL().openStream(), StandardCharsets.UTF_8)
            ).use { reader ->
                val json = GsonHelper.fromJson(Transition.GSON, reader, JsonObject::class.java)
                val mappings = Mappings.CODEC.parse(JsonOps.INSTANCE, json)
                if (mappings.isError) {
                    Transition.LOGGER.error(mappings.error().get().message())
                    return false
                }
                ACTIVE_MAPPINGS[modId] = mappings.orThrow
                Transition.log("Loaded mappings for: $modId")
            }
        } catch (e: IOException) {
            error(e)
        }

        return true
    }

    @JvmField
    val ACTIVE_MAPPINGS = mutableMapOf<String, Mappings>()
}
