package org.teamvoided.transition.api.data.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataPackOutput;
import net.minecraft.registry.HolderLookup;
import net.minecraft.util.Identifier;
import org.teamvoided.transition.mappings.Mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public abstract class MappingsProvider extends FabricCodecDataProvider<Mappings> {

    protected MappingsProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture, DataPackOutput.Type.RESOURCE_PACK, "", Mappings.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, Mappings> provider, HolderLookup.Provider lookup) {
        var builder = new MappingBuilder();
        makeMappings(lookup, builder);
        provider.accept(Identifier.of("transitions", "mappings"), builder.build());

    }

    abstract void makeMappings(HolderLookup.Provider lookup, MappingBuilder builder);

    @Override
    public String getName() {
        return "transitions:mappings";
    }

    @SuppressWarnings("unused")
    public static class MappingBuilder {
        private final HashSet<String> oldNamespaces = new HashSet<>();
        private final HashMap<String, String> oldToNewPaths = new HashMap<>();

        void addOldNamespace(String from) {
            oldNamespaces.add(from);
        }

        void addOldPathMapping(Identifier to, String from) {
            oldToNewPaths.put(from, to.toString());
        }

        void addOldPathMappings(Identifier to, String... from) {
            for (String s : from) {
                addOldPathMapping(to, s);
            }
        }

        Mappings build() {
            return new Mappings(oldNamespaces.stream().toList(), oldToNewPaths);
        }
    }
}
