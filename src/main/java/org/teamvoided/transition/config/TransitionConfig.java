package org.teamvoided.transition.config;

import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.ResourceLocation;
import org.teamvoided.transition.Transition;

import java.util.List;

public class TransitionConfig extends Config {

    public MappingModes mode = MappingModes.ON_LOAD;
    public List<String> directoryBlackList = List.of("datapacks", "mods", "config");

    public TransitionConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Transition.MODID, "main"));
    }
}
