package org.teamvoided.transition.config

import me.fzzyhmstrs.fzzy_config.config.Config
import org.teamvoided.transition.Transition.id

class TransitionConfig : Config(id("main")) {
    @JvmField
    var mode = MappingModes.ON_LOAD
    var directoryBlackList: MutableList<String> = mutableListOf("datapacks", "mods", "config")
}
