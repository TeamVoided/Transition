package org.teamvoided.transition.mappings;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.teamvoided.transition.Transition.GSON;

public interface MappingManager {
    Map<String, Mappings> list = new HashMap<>();

    static void loadModMappings(ModContainer mod) {
        mod.findPath("mappings.json").ifPresent((path) -> {
            var file = path.toFile();
            if (file.exists()) {
                try {
                    String mappingsString = Files.readString(path);
                    var mappings = GSON.fromJson(mappingsString, Mappings.class);
                    list.put(mod.getMetadata().getId(), mappings);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    static void death() {
        try {

            Path filePath = FabricLoader.getInstance().getGameDir().resolve("not_cache.json");
            var t = new Mappings(
                    List.of("yello", "2_6"),
                    Map.of("test_block", "tset_b", "tes", "test")
            );
            Files.writeString(filePath, GSON.toJson(t), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
