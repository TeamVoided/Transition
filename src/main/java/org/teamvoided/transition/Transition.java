package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamvoided.transition.mappings.MappingManager;


@SuppressWarnings("unused")
public class Transition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Transition");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean IS_ACTIVE = false;

    @Override
    public void onInitialize() {
        MappingManager.mappingTester();
        CacheManager.readCache();
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("remapping")) {
                boolean enabled = metadata.getCustomValue("remapping").getAsBoolean();
                if (enabled) {
                    CacheManager.updateCache(mod);
                    MappingManager.loadModMappings(mod);
                }
            }
        });
        CacheManager.writeCache();
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) System.exit(0);
    }
}
