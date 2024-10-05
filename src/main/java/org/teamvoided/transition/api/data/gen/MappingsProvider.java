package org.teamvoided.transition.api.data.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.teamvoided.transition.CacheManager;
import org.teamvoided.transition.Transition;
import org.teamvoided.transition.mappings.Mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public abstract class MappingsProvider extends FabricCodecDataProvider<Mappings> {
    public MappingsProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture, PackOutput.Target.DATA_PACK, "", Mappings.CODEC);
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, Mappings> provider, HolderLookup.Provider lookup) {
        var builder = new MappingBuilder();
        makeMappings(lookup, builder);
        provider.accept(ResourceLocation.fromNamespaceAndPath(Transition.MODID, "mappings"), builder.build());
    }

    public abstract void makeMappings(HolderLookup.Provider lookup, MappingBuilder builder);

    @Override
    public @NotNull String getName() {
        return "transitions:mappings";
    }

    @SuppressWarnings("unused")
    public static class MappingBuilder {
        private final Map<String, Map<String, String>> mappings = new HashMap<>();

        public NamespaceBuilder namespace(String namespace) {
            return new NamespaceBuilder(this, namespace);
        }

        public Mappings build() {
            return new Mappings(mappings);
        }

        public static class NamespaceBuilder {
            final String namespace;
            private final MappingBuilder parent;
            private final Map<String, String> namespaceMappings = new HashMap<>();

            NamespaceBuilder(MappingBuilder parent, String namespace) {
                this.parent = parent;
                this.namespace = namespace;
            }

            public NamespaceBuilder addPathMapping(String from, String to) {
                namespaceMappings.put(from, to);
                return this;
            }

            public MappingBuilder build() {
                parent.mappings.put(namespace, namespaceMappings);
                return parent;
            }
        }
    }
}
