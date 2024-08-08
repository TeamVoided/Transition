package org.teamvoided.transition.mappings;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.teamvoided.transition.Transition.GSON;
import static org.teamvoided.transition.Transition.LOGGER;

public interface MappingManager {
    Map<String, Mappings> ACTIVE_MAPPINGS = new HashMap<>();

    static void loadModMappings(ModContainer mod) {
        mod.findPath("mappings.json").ifPresent((path) -> {
            var file = path.toFile();
            if (file.exists()) {
                try {
                    JsonObject json = JsonHelper.deserialize(GSON, Files.readString(path), JsonObject.class);

                    Mappings.CODEC.parse(JsonOps.INSTANCE, json)
                            .resultOrPartial(LOGGER::error)
                            .ifPresent((mapping) -> ACTIVE_MAPPINGS.put(mod.getMetadata().getId(), mapping));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    static void mappingTester() {
        try {
            Path filePath = FabricLoader.getInstance().getGameDir().resolve("not_cache.json");
            var t = new Mappings(
                    List.of("yello", "2_6"),
                    Map.of("test_block", "tset_b", "tes", "test")
            );
            Files.writeString(filePath, GSON.toJson(Mappings.CODEC.encodeStart(JsonOps.INSTANCE, t).getOrThrow()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
