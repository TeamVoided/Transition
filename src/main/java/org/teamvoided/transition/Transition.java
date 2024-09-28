package org.teamvoided.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamvoided.transition.mappings.MappingManager;

import static org.teamvoided.transition.ServerProcessor.processDirectory;


@SuppressWarnings("unused")
public class Transition implements ModInitializer, DedicatedServerModInitializer {

    public static final String MODID = "transition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean IS_ACTIVE = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing main");
        loadMod();
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Initializing server");
        loadMod();
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    }

    public void onServerStarting(MinecraftServer server) {
        var worldFile = server.getWorldPath(LevelResource.ROOT).toFile();
        LOGGER.info("Server world worldFile: {}", worldFile);
        processDirectory(worldFile);

        server.close();
    }


    public static void loadMod() {
        CacheManager.readCache();
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("remapping")) {
                boolean enabled = metadata.getCustomValue("remapping").getAsBoolean();
                if (enabled) {
                    LOGGER.info("Mod \"{}\" has remapping enabled", metadata.getId());
                    CacheManager.updateCache(metadata);
                    MappingManager.loadModMappings(mod);
                }
            }
        });
        CacheManager.writeCache();
    }
}
