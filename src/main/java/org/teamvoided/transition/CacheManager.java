package org.teamvoided.transition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.teamvoided.transition.Transition.LOGGER;

public class CacheManager {

    public static final File CACHE_FILE = FabricLoader.getInstance().getGameDir().resolve("cache.json").toFile();
    private static final Codec<List<Pair<String, String>>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.pair(Codec.STRING, Codec.STRING).listOf().fieldOf("mods").forGetter(list -> list)
    ).apply(instance, list -> list));
    public static final Map<String, String> CACHED_MODS = Util.make(new HashMap<>(), map -> {
        map.put("test_mod", "1.0.0");
    });

    public static void updateCache(ModContainer mod) {
        ModMetadata metadata = mod.getMetadata();
        String modId = metadata.getId();

        if (CACHED_MODS.containsKey(modId)) {
            String version = CACHED_MODS.get(modId);
            String newVersion = mod.getMetadata().getVersion().getFriendlyString();
            if (!version.equals(newVersion)) {
                LOGGER.info("{} -> {}", version, newVersion);
            }
        }
    }

    public static void readCache() {
        if (CACHE_FILE.exists()) {
            try (Reader reader = new BufferedReader(new InputStreamReader(CACHE_FILE.toURI().toURL().openStream(), StandardCharsets.UTF_8))) {
                JsonObject json = JsonHelper.deserialize(Transition.GSON, reader, JsonObject.class);

                CACHED_MODS.clear();
                CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent(cachedMods -> cachedMods.forEach(pair ->
                                CACHED_MODS.put(pair.getFirst(), pair.getSecond()))
                        );
            }
            catch (IOException e) {
                LOGGER.error("Failed to read cache file", e);
            }
        }
    }

    public static void writeCache() {
        try {
            Files.deleteIfExists(CACHE_FILE.toPath());
            List<Pair<String, String>> map = new ArrayList<>();

            CACHED_MODS.forEach((modId, version) -> map.add(Pair.of(modId, version)));
            JsonElement element = CODEC.encodeStart(JsonOps.INSTANCE, map).getOrThrow();
            Files.writeString(CACHE_FILE.toPath(), Transition.GSON.toJson(element), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write cache file", e);
        }
    }
}
