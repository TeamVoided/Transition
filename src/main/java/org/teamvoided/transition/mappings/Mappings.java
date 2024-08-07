package org.teamvoided.transition.mappings;


import java.util.List;
import java.util.Map;

public record Mappings(List<String> oldNamespaces, Map<String, String> oldToNewPaths) {

//    static Codec<Mappings> CODEC = RecordCodecBuilder.create((instance) ->
//            instance.group(
//            Codec.STRING.listOf().fieldOf("oldNamespaces").forGetter((config) -> config.oldNamespaces),
//            RecordCodecBuilder.mapCodec()
//                    .codec().fieldOf("oldToNewPaths").forGetter((config) -> config.oldToNewPaths)
//    ).apply(instance, Mappings::new));
}