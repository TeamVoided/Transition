package org.teamvoided.transition.mappings;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.teamvoided.transition.api.misc.MapCodecs;

import java.util.List;
import java.util.Map;

public record Mappings(List<String> oldNamespaces, Map<String, String> oldToNewPaths) {

    public static final Codec<Mappings> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.STRING.listOf().fieldOf("oldNamespaces").forGetter(Mappings::oldNamespaces),
                    MapCodecs.codec(
                            Codec.pair(Codec.STRING.fieldOf("old").codec(), Codec.STRING.fieldOf("new").codec())
                    ).fieldOf("oldToNewPaths").forGetter(Mappings::oldToNewPaths)
            )
            .apply(instance, Mappings::new)
    );
}