package org.teamvoided.transition;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.teamvoided.transition.mappings.MappingsManager;
import org.teamvoided.transition.utils.RegionFileIO;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

import static net.minecraft.world.level.chunk.storage.RegionFileStorage.ANVIL_EXTENSION;
import static org.teamvoided.transition.Transition.LOGGER;
import static org.teamvoided.transition.Transition.log;

public interface ServerProcessor {
    static void processDirectory(File directory) {
        log("Processing directory: %s".formatted(directory.getName()));
        if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory");
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) processDirectory(file);
            else {
                var name = file.getName();
                if (name.endsWith(".dat")/*|| name.endsWith(".dat_old")*/) processDatFile(file);
                else if (name.endsWith(ANVIL_EXTENSION)) processMcaFile(file);
            }
        }
    }


    static void processDatFile(File datFile) {
        try {
            log(" |- Processing .dat File: %s".formatted(datFile.getName()));
            var tag = NbtIo.readCompressed(datFile.toPath(), NbtAccounter.unlimitedHeap());
            var newTag = processCompoundTag(tag);
            if (newTag != null) {
                log("   |- Updating: %s".formatted(datFile.getName()));
                NbtIo.writeCompressed(newTag, datFile.toPath());
            }
        } catch (IOException e) {
            LOGGER.error("!  |- Failed to read datFile: {}", datFile, e);
        }
    }

    static void processMcaFile(File file) {
        log(" |- Processing .mca File: %s".formatted(file.getName()));
        try {
            var x = RegionFileIO.read(file);
            x.forEach((chunkPos, chunkNbt) -> {
                var newTag = processCompoundTag(chunkNbt);
                if (newTag != null) {
                    log("   |- Updating ChunkPos: %s".formatted(chunkPos));
                    try {
                        RegionFileIO.write(file, chunkPos, newTag);
                    } catch (IOException e) {
                        LOGGER.error("!  |- Failed to write ChunkPos[{}]: {}", chunkPos, e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("!  |- Failed to read Fil! {}", file, e);
        }

    }

    static CompoundTag processCompoundTag(CompoundTag tag) {
        final boolean[] updated = {false};
        tag.getAllKeys().forEach(key -> {
            var value = tag.get(key);
            assert value != null;
            if (value.getType() == CompoundTag.TYPE) {
                var newTag = processCompoundTag((CompoundTag) value);
                if (newTag != null) {
                    updated[0] = true;
                    tag.put(key, newTag);
                }
            } else if (value.getType() == ListTag.TYPE) {
                var newList = processListTag((ListTag) value);
                if (newList != null) {
                    updated[0] = true;
                    tag.put(key, newList);
                }
            } else if (value.getType() == StringTag.TYPE) {
                var newString = modifyString(value.getAsString());
                if (newString != null) {
                    updated[0] = true;
                    tag.putString(key, newString);
                }
            }
        });
        return updated[0] ? tag : null;
    }

    static ListTag processListTag(ListTag listTag) {
        final boolean[] updated = {false};
        var type = listTag.getElementType();
        BiConsumer<Integer, Tag> fun = switch (type) {
            case Tag.TAG_COMPOUND -> (i, value) -> {
                var newTag = processCompoundTag((CompoundTag) value);
                if (newTag != null) {
                    updated[0] = true;
                    listTag.set(i, newTag);
                }
            };
            case Tag.TAG_LIST -> (i, value) -> {
                var newList = processListTag((ListTag) value);
                if (newList != null) {
                    updated[0] = true;
                    listTag.set(i, newList);
                }
            };
            case Tag.TAG_STRING -> (i, value) -> {
                var newString = modifyString(value.getAsString());
                if (newString != null) {
                    updated[0] = true;
                    listTag.set(i, StringTag.valueOf(newString));
                }
            };
            default -> (i, value) -> {
            };
        };

        for (int idx = 0; idx < listTag.size(); idx++) {
            var tag = listTag.get(idx);
            fun.accept(idx, tag);
        }
        return updated[0] ? listTag : null;
    }

    static String modifyString(String input) {
        if (input.contains(":")) {
            var id = ResourceLocation.tryParse(input);
            if (id != null) {
                var namespace = new String[]{id.getNamespace()};
                if (namespace[0].equals(Transition.MINECRAFT)) return null;

                var path = new String[]{id.getPath()};
                MappingsManager.ACTIVE_MAPPINGS.forEach((currentNamespace, mapping) -> {
                    if (!id.getNamespace().equals(currentNamespace) && mapping.oldNamespaces().contains(id.getNamespace())) {
                        namespace[0] = currentNamespace;
                    }
                    if (namespace[0].equals(currentNamespace) && mapping.oldToNewPaths().containsKey(id.getPath())) {
                        path[0] = mapping.oldToNewPaths().get(id.getPath());
                    }
                });
                var newId = ResourceLocation.fromNamespaceAndPath(namespace[0], path[0]);
                if (FabricLoader.getInstance().isDevelopmentEnvironment() && !id.equals(newId))
                    log("    |- Old id: %s, New id: %s".formatted(id, newId));

                return id != newId ? newId.toString() : null;
            }
        }
        return null;
    }

}
