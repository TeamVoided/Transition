package org.teamvoided.transition;

import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.ResourceLocation;
import org.teamvoided.transition.mappings.MappingModes;

import java.util.List;

public class TransitionConfig extends Config {

    public MappingModes mode = MappingModes.ON_LOAD;
    public List<String> directoryBlackList = List.of("datapacks");

    public TransitionConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Transition.MODID, "main"));
    }
}
