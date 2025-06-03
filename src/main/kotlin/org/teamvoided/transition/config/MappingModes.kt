package org.teamvoided.transition.config

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable
import org.teamvoided.transition.Transition.MODID

enum class MappingModes : EnumTranslatable {
    OFF,
    ON_LOAD,
    CONTINUOUS;

    override fun prefix(): String = "$MODID.main.mode"
}
