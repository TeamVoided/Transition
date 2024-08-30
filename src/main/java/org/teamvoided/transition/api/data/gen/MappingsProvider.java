package org.teamvoided.transition.api.data.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.teamvoided.transition.mappings.Mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public abstract class MappingsProvider extends FabricCodecDataProvider<Mappings> {
    public MappingsProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture, PackOutput.Target.RESOURCE_PACK, "", Mappings.CODEC);
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, Mappings> provider, HolderLookup.Provider lookup) {
        var builder = new MappingBuilder();
        makeMappings(lookup, builder);
        provider.accept(ResourceLocation.fromNamespaceAndPath("transitions", "mappings"), builder.build());

    }

    public abstract void makeMappings(HolderLookup.Provider lookup, MappingBuilder builder);

    @Override
    public @NotNull String getName() {
        return "transitions:mappings";
    }

    @SuppressWarnings("unused")
    public static class MappingBuilder {
        private final HashSet<String> oldNamespaces = new HashSet<>();
        private final HashMap<String, String> oldToNewPaths = new HashMap<>();

        public void addOldNamespace(String from) {
            oldNamespaces.add(from);
        }

        public void addOldPathMapping(String to, String from) {
            oldToNewPaths.put(from, to);
        }

        public void addOldPathMappings(String to, String... from) {
            for (String s : from) {
                addOldPathMapping(to, s);
            }
        }

        public Mappings build() {
            return new Mappings(oldNamespaces.stream().toList(), oldToNewPaths);
        }
    }
}
