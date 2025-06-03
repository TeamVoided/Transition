package org.teamvoided.transition.mappings

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.teamvoided.transition.api.misc.MapCodecs

@JvmRecord
data class Mappings(
    @JvmField val oldNamespaces: MutableList<String>, @JvmField val oldToNewPaths: MutableMap<String, String>,
) {
    companion object {
        val CODEC: Codec<Mappings> =
            RecordCodecBuilder.create {
                it
                    .group(
                        Codec.STRING.listOf()
                            .optionalFieldOf("oldNamespaces", mutableListOf())
                            .forGetter(Mappings::oldNamespaces),
                        MapCodecs.codec(
                            Codec.pair(Codec.STRING.fieldOf("old").codec(), Codec.STRING.fieldOf("new").codec())
                        )
                            .optionalFieldOf("oldToNewPaths", mutableMapOf())
                            .forGetter(Mappings::oldToNewPaths)
                    )
                    .apply(it, ::Mappings)
            }
    }
}