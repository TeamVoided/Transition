package org.teamvoided.transition.mappings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.teamvoided.transition.api.misc.MapCodecs;

import java.util.List;
import java.util.Map;

public record Mappings(Map<String, Map<String, String>> mappings) {

    public static final Codec<Mappings> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.STRING)).fieldOf("mappings").forGetter(Mappings::mappings)
            )
            .apply(instance, Mappings::new)
    );
}