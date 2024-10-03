package org.teamvoided.transition.mappings;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import org.jetbrains.annotations.NotNull;
import org.teamvoided.transition.Transition;

public enum MappingModes implements EnumTranslatable {
    OFF,
    ON_LOAD,
    CONTINUOUS;

    @NotNull
    @Override
    public String prefix() {
        return Transition.MODID + ".main.mode";
    }
}
