package org.teamvoided.transition.mappings;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.teamvoided.transition.Transition.GSON;
import static org.teamvoided.transition.Transition.LOGGER;

public interface MappingManager {
    Map<String, Mappings> ACTIVE_MAPPINGS = new HashMap<>();

    static void loadModMappings(ModContainer mod) {
        var metadata = mod.getMetadata();
        mod.findPath("assets/transition/mappings.json").ifPresent((path) -> {
            var file = path.toFile();
            if (file.exists()) {
                try {
                    JsonObject json = GsonHelper.fromJson(GSON, Files.readString(path), JsonObject.class);
                    Mappings.CODEC.parse(JsonOps.INSTANCE, json)
                            .resultOrPartial(LOGGER::error)
                            .ifPresent((mapping) -> ACTIVE_MAPPINGS.put(metadata.getId(), mapping));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.error("Mod has mappings Enabled, But no mappings.json found for mod \"{}\"!", metadata.getId());
            }
        });
    }
}
