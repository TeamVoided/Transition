package org.teamvoided.transition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.GsonHelper;
import org.teamvoided.transition.api.misc.MapCodecs;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.teamvoided.transition.Transition.*;

public interface CacheManager {

    File CACHE_FILE = FabricLoader.getInstance().getGameDir().resolve("data").resolve("transition_cache").toFile();

    Codec<Map<String, String>> CODEC = MapCodecs.codec(Codec.pair(
            Codec.STRING.fieldOf("id").codec(),
            Codec.STRING.fieldOf("version").codec()
    ));

    Map<String, String> CACHED_MODS = new HashMap<>();

    static void updateCache(ModMetadata metadata) {
        String modId = metadata.getId();

        if (CACHED_MODS.containsKey(modId)) {
            String version = CACHED_MODS.get(modId);
            String newVersion = metadata.getVersion().getFriendlyString();
            if (!version.equals(newVersion)) {
                log("%s -> %s".formatted(version, newVersion));
                Transition.IS_ACTIVE = true;
                CACHED_MODS.put(modId, newVersion);
            }
        } else {
            var type = metadata.getType();
            if (!Objects.equals(type, "builtin")) {
                Transition.IS_ACTIVE = true;
                CACHED_MODS.put(modId, metadata.getVersion().getFriendlyString());
            }
        }
    }

    static void readCache() {
        if (CACHE_FILE.exists()) {
            try (Reader reader = new BufferedReader(new InputStreamReader(CACHE_FILE.toURI().toURL().openStream(), StandardCharsets.UTF_8))) {
                JsonArray json = GsonHelper.fromJson(GSON, reader, JsonArray.class);

                CACHED_MODS.clear();
                CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent(CACHED_MODS::putAll);
            } catch (IOException e) {
                LOGGER.error("Failed to read cache file", e);
            }
        }
    }

    static void writeCache() {
        try {
            Files.deleteIfExists(CACHE_FILE.toPath());
            JsonElement element = CODEC.encodeStart(JsonOps.INSTANCE, CACHED_MODS).getOrThrow();
            Files.writeString(CACHE_FILE.toPath(), GSON.toJson(element), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to write cache file", e);
        }
    }
}
