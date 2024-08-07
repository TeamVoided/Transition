package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class Transition implements ModInitializer {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Codec<List<ModData>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModData.CODEC.listOf().fieldOf("mods").forGetter(list -> list)
    ).apply(instance, list -> list));

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("remapping")) {
                boolean enabled = metadata.getCustomValue("remapping").getAsBoolean();
            }
        });

        File gameDir = FabricLoader.getInstance().getGameDir().resolve("cache").toFile();
        try {
            JsonObject json = JsonHelper.deserialize(GSON, new BufferedReader(new InputStreamReader(gameDir.toURI().toURL().openStream(), StandardCharsets.UTF_8)), JsonObject.class);

            Optional<List<ModData>> decodedList = CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> System.out.println("Failed to decode file"));
            List<ModData> data = decodedList.orElse(new ArrayList<>());

            data.forEach(System.out::println);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record ModData(String modId, String version) {
        private static final Codec<ModData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("mod_id").forGetter(ModData::modId),
                Codec.STRING.fieldOf("version").forGetter(ModData::version)
        ).apply(instance, ModData::new));
    }
}
