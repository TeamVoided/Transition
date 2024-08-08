package org.teamvoided.transition.api.misc;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import java.util.Map;

public interface MapCodecs {
    static <F, S> Codec<Map<F, S>> codec(Codec<Pair<F, S>> pair) {
        return pair.listOf().xmap(
                list -> list.stream().collect(Pair.toMap()),
                map -> map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList()
        );
    }
}
