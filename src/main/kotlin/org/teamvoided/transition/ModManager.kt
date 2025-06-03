package org.teamvoided.transition

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.util.GsonHelper
import org.teamvoided.transition.api.misc.MapCodecs
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object ModManager {
    fun addActiveMod(metadata: ModMetadata) {
        val modId = metadata.id

        val version = ACTIVE_MODS[modId]
        val newVersion = metadata.version.friendlyString
        if (newVersion == null) {
            Transition.error("No version provided for mod $modId")
            return
        }

        if (version != null && version != newVersion) {
            Transition.log("[$modId]:  $version -> $newVersion")
            Transition.IS_ACTIVE = true
            ACTIVE_MODS.put(modId, newVersion)
        } else if (metadata.type != "builtin") {
            Transition.IS_ACTIVE = true
            ACTIVE_MODS.put(modId, metadata.version.friendlyString)
        }
    }

    fun readCache() {
        if (CACHE_FILE.exists()) {
            try {
                BufferedReader(
                    InputStreamReader(
                        CACHE_FILE.toURI().toURL().openStream(),
                        StandardCharsets.UTF_8
                    )
                ).use { reader ->
                    val json =
                        GsonHelper.fromJson(Transition.GSON, reader, JsonArray::class.java)
                    ACTIVE_MODS.clear()
                    CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial { Transition.LOGGER.error(it) }
                        .ifPresent { ACTIVE_MODS.putAll(it) }
                }
            } catch (e: IOException) {
                Transition.LOGGER.error("Failed to read cache file", e)
            }
        }
    }

    fun writeCache() {
        val path: Path = CACHE_FILE.toPath()
        try {
            if (Files.exists(path)) {
                Files.deleteIfExists(CACHE_FILE.toPath())
            } else {
                Files.createDirectories(path)
            }

            val element: JsonElement = CODEC.encodeStart(JsonOps.INSTANCE, ACTIVE_MODS).getOrThrow()
            Files.writeString(path, Transition.GSON.toJson(element), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            Transition.LOGGER.error("Failed to write cache file", e)
        }
    }

    val CACHE_FILE: File =
        FabricLoader.getInstance().gameDir.resolve("data").resolve("transition_cache").toFile()

    val CODEC: Codec<MutableMap<String, String>> = MapCodecs
        .codec(Codec.pair(Codec.STRING.fieldOf("id").codec(), Codec.STRING.fieldOf("version").codec()))

    val ACTIVE_MODS = mutableMapOf<String, String>()
}
