package org.teamvoided.transition.mappings;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
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
    Map<String, Pair<ModContainer, Mappings>> ACTIVE_MAPPINGS = new HashMap<>();

    static void loadModMappings(ModContainer mod, String modId) {
        var metadata = mod.getMetadata();
        mod.findPath("data/" + Transition.MODID + "/mappings.json").ifPresentOrElse((path) -> {
            try (Reader reader = new BufferedReader(new InputStreamReader(path.toUri().toURL().openStream(), StandardCharsets.UTF_8))) {
                JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                var mappings = Mappings.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(LOGGER::error).orElse(null);
                if (mappings == null) { return; }

                mappings.mappings().forEach((oldNamespace, map) -> {
                    if (ACTIVE_MAPPINGS.containsKey(oldNamespace)) {
                        var oldMod = ACTIVE_MAPPINGS.get(oldNamespace).getFirst();
                        LOGGER.warn("Duplicate namespace {} in mods: {}, {}", oldNamespace, metadata.getId(), oldMod.getMetadata().getId());
                    }

                    ACTIVE_MAPPINGS.put(oldNamespace, new Pair<>(mod, mappings));
                });

                log("Loaded mappings for: %s".formatted(modId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, () -> LOGGER.error("Failed to find mappings file for: {}", modId));
    }
}
