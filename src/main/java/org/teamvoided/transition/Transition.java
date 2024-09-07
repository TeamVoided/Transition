package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamvoided.transition.mappings.MappingsManager;


@SuppressWarnings("unused")
public class Transition implements ModInitializer {

    public static final String MODID = "transition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean IS_ACTIVE = false;

    @Override
    public void onInitialize() {
        CacheManager.readCache();
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("remapping")) {
                boolean enabled = metadata.getCustomValue("remapping").getAsBoolean();
                if (enabled) {
                    LOGGER.info("Mod \"{}\" has remapping enabled", metadata.getId());
                    CacheManager.updateCache(metadata);
                    MappingsManager.loadModMappings(mod, metadata.getId());
                }
            }
        });
        CacheManager.writeCache();
//        if (FabricLoader.getInstance().isDevelopmentEnvironment()) System.exit(0);
    }
}
