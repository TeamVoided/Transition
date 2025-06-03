package org.teamvoided.transition.api.misc

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec

object MapCodecs {
    fun <F, S> codec(pair: Codec<Pair<F, S>>): Codec<MutableMap<F, S>> {
        return pair.listOf().xmap({ it.stream().collect(Pair.toMap()) })
        { map -> map.entries.stream().map { Pair.of(it.key, it.value) }.toList() }
    }
}
