package org.teamvoided.transition;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unused")
public class Transition implements ModInitializer {
    @Override
    public void onInitialize() {

        FabricLoader.getInstance().getAllMods().forEach((mod)->{
            System.out.println(mod.getMetadata().getId() + " " + mod.getMetadata().getVersion().getFriendlyString());
        });

    }
}
