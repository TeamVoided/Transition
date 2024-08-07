package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
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

@SuppressWarnings("unused")
public class Transition implements ModInitializer {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Codec<List<CachedMod>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CachedMod.CODEC.listOf().fieldOf("mods").forGetter(list -> list)
    ).apply(instance, list -> list));
    public static final List<CachedMod> CACHED_MODS = Util.make(new ArrayList<>(), list -> {
        list.add(new CachedMod("test_mod", "1.0.0"));
    });

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("remapping")) {
                boolean enabled = metadata.getCustomValue("remapping").getAsBoolean();
            }
        });

        File gameDir = FabricLoader.getInstance().getGameDir().resolve("cache.json").toFile();

        try {
            if (gameDir.exists()) {
                JsonObject json = JsonHelper.deserialize(GSON, new BufferedReader(new InputStreamReader(gameDir.toURI().toURL().openStream(), StandardCharsets.UTF_8)), JsonObject.class);

                CACHED_MODS.clear();
                CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> System.out.println("Failed to decode file"))
                        .ifPresent(CACHED_MODS::addAll);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            JsonElement element = CODEC.encodeStart(JsonOps.INSTANCE, CACHED_MODS).getOrThrow();
            Files.writeString(gameDir.toPath(), GSON.toJson(element), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    public record CachedMod(String modId, String version) {

        private static final Codec<CachedMod> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("mod_id").forGetter(CachedMod::modId),
                Codec.STRING.fieldOf("version").forGetter(CachedMod::version)
        ).apply(instance, CachedMod::new));
    }
}
