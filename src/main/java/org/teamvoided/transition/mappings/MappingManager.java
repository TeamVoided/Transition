package org.teamvoided.transition.mappings;

import com.google.gson.Gson;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public interface MappingManager {
    Map<String, Mappings> list = new HashMap<>();

    Gson gson = new Gson();

    static void loadModMappings(ModContainer mod) {
        mod.findPath("mappings.json").ifPresent((path) -> {
            var file = path.toFile();
            if (file.exists()) {
                try {
                    String mappingsString = Files.readString(path);
                    var mappings = gson.fromJson(mappingsString, Mappings.class);
                    list.put(mod.getMetadata().getId(), mappings);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
