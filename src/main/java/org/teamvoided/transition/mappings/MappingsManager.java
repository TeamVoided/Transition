package org.teamvoided.transition.mappings;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.GsonHelper;
import org.teamvoided.transition.Transition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.teamvoided.transition.Transition.*;

public interface MappingsManager {

    Map<String, Mappings> ACTIVE_MAPPINGS = new HashMap<>();

    static void loadModMappings(ModContainer mod, String modId, boolean hasRemapping) {
        var metadata = mod.getMetadata();
        mod.findPath("data/" + Transition.MODID + "/mappings.json").ifPresentOrElse((path) -> {
            try (Reader reader = new BufferedReader(new InputStreamReader(path.toUri().toURL().openStream(), StandardCharsets.UTF_8))) {
                JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                Mappings.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent((mapping) -> ACTIVE_MAPPINGS.put(metadata.getId(), mapping));
                log("Loaded mappings for: %s".formatted(modId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            if (hasRemapping) LOGGER.error("Failed to find mappings file for: {}", modId);
        });
    }
}
