package org.teamvoided.transition;

import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.ResourceLocation;
import org.teamvoided.transition.mappings.MappingModes;

public class TransitionConfig extends Config {

    public MappingModes mode = MappingModes.ON_LOAD;

    public TransitionConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Transition.MODID, "main"));
    }
}
