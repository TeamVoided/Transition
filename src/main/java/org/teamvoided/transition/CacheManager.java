package org.teamvoided.transition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.teamvoided.transition.Transition.LOGGER;

public class CacheManager {

    public static final File CACHE_FILE = FabricLoader.getInstance().getGameDir().resolve("cache.json").toFile();
    private static final Codec<List<CachedMod>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CachedMod.CODEC.listOf().fieldOf("mods").forGetter(list -> list)
    ).apply(instance, list -> list));
    public static final List<CachedMod> CACHED_MODS = Util.make(new ArrayList<>(), list -> {
        list.add(new CachedMod("test_mod", "1.0.0"));
    });

    public static void readCache() {
        try {
            if (CACHE_FILE.exists()) {
                JsonObject json = JsonHelper.deserialize(Transition.GSON, new BufferedReader(new InputStreamReader(CACHE_FILE.toURI().toURL().openStream(), StandardCharsets.UTF_8)), JsonObject.class);

                CACHED_MODS.clear();
                CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent(CACHED_MODS::addAll);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeCache() {
        try {
            Files.deleteIfExists(CACHE_FILE.toPath());
            JsonElement element = CODEC.encodeStart(JsonOps.INSTANCE, CACHED_MODS).getOrThrow();
            Files.writeString(CACHE_FILE.toPath(), Transition.GSON.toJson(element), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write cache file", e);
        }
    }

    public record CachedMod(String modId, String version) {

        private static final Codec<CachedMod> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("mod_id").forGetter(CachedMod::modId),
                Codec.STRING.fieldOf("version").forGetter(CachedMod::version)
        ).apply(instance, CachedMod::new));
    }
}
