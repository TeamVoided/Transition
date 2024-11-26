package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamvoided.transition.mappings.MappingModes;
import org.teamvoided.transition.mappings.MappingsManager;

import static org.teamvoided.transition.ServerProcessor.processDirectory;


@SuppressWarnings("unused")
public class Transition implements ModInitializer {

    public static final String MODID = "transition";
    public static final String MINECRAFT = "minecraft";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final TransitionConfig CONFIG = ConfigApiJava.registerAndLoadConfig(TransitionConfig::new);

    public static boolean IS_ACTIVE = false;

    @Override
    public void onInitialize() {
        log("Transitioning Transition!");
        loadMod();
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    }

    public void onServerStarting(MinecraftServer server) {
        if (CONFIG.mode == MappingModes.ON_LOAD) {
            var worldFile = server.getWorldPath(LevelResource.ROOT).toFile();
            log("Server world worldFile: %s".formatted(worldFile));
            processDirectory(worldFile);
            log("Finished processing directory");

            server.stopServer();
        }
    }

    public static void loadMod() {
        if (CONFIG.mode == MappingModes.OFF) {
            return;
        }

//        CacheManager.readCache();
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            MappingsManager.loadModMappings(mod, metadata.getId(), metadata.containsCustomValue("has_remapping"));
//            CacheManager.updateCache(metadata);
        });
//        CacheManager.writeCache();
    }

    public static void log(String message) {
        LOGGER.info("(Transition) {}", message);
    }

    public static void error(String message) {
        LOGGER.error("(Transition) {}", message);
    }
}
