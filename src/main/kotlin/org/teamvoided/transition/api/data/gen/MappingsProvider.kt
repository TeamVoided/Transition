package org.teamvoided.transition.api.data.gen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import org.teamvoided.transition.Transition.id
import org.teamvoided.transition.mappings.Mappings
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

@Suppress("unused")
abstract class MappingsProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricCodecDataProvider<Mappings>(
    dataOutput, registriesFuture, PackOutput.Target.DATA_PACK, "", Mappings.Companion.CODEC
) {
    override fun configure(provider: BiConsumer<ResourceLocation, Mappings>, lookup: HolderLookup.Provider) {
        val builder = MappingBuilder()
        makeMappings(lookup, builder)
        provider.accept(id("mappings"), builder.build())
    }

    abstract fun makeMappings(lookup: HolderLookup.Provider, builder: MappingBuilder)
    override fun getName(): String = "transitions:mappings"
    class MappingBuilder {
        private val oldNamespaces = mutableSetOf<String>()
        private val oldToNewPaths = mutableMapOf<String, String>()

        fun addOldNamespace(from: String) {
            oldNamespaces.add(from)
        }

        fun addOldPathMapping(to: String, from: String) {
            oldToNewPaths.put(from, to)
        }

        fun addOldPathMappings(to: String, vararg from: String) {
            for (s in from) {
                addOldPathMapping(to, s)
            }
        }

        fun build(): Mappings {
            return Mappings(oldNamespaces.stream().toList(), oldToNewPaths)
        }
    }
}
