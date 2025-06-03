package org.teamvoided.transition

import net.minecraft.nbt.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFileStorage
import org.teamvoided.transition.Transition.isDev
import org.teamvoided.transition.mappings.MappingsManager
import org.teamvoided.transition.utils.RegionFileIO
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

object ServerProcessor {
    fun processDirectory(directory: File) {
        Transition.log("Processing directory: ${directory.name}")
        require(directory.isDirectory()) { "Not a directory" }
        for (file in Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory() && !Transition.CONFIG.directoryBlackList.contains(file.getName())) {
                processDirectory(file)
            } else {
                val name = file.getName()
                if (name.endsWith(".dat") || name.endsWith(".dat_old")) processDatFile(file)
                else if (name.endsWith(RegionFileStorage.ANVIL_EXTENSION)) processMcaFile(file)
            }
        }
    }


    fun processDatFile(datFile: File) {
        try {
            Transition.log(" |- Processing .dat File: ${datFile.name}")
            val tag = NbtIo.readCompressed(datFile.toPath(), NbtAccounter.unlimitedHeap())
            val newTag = processCompoundTag(tag)
            if (newTag != null) {
                Transition.log("   |- Updating: ${datFile.name}")
                NbtIo.writeCompressed(newTag, datFile.toPath())
            }
        } catch (e: IOException) {
            Transition.error("!  |- Failed to read .dat File $datFile: $e")
        }
    }

    fun processMcaFile(file: File) {
        Transition.log(" |- Processing .mca File: ${file.name}")
        try {
            val processedChunks = ArrayList<Any>()
            val chunkData: HashMap<ChunkPos, CompoundTag> = RegionFileIO.read(file)
            chunkData.forEach { (chunkPos: ChunkPos, chunkNbt: CompoundTag) ->
                val newTag = processCompoundTag(chunkNbt)
                if (newTag != null) {
                    try {
                        RegionFileIO.write(file, chunkPos, newTag)
                        processedChunks.add(chunkPos)
                    } catch (e: IOException) {
                        Transition.error("!  |- Failed to write ChunkPos[$chunkPos]: $e")
                    }
                }
            }
            Transition.log("   |- Updating ChunkPos: $processedChunks")
        } catch (e: IOException) {
            Transition.error("!  |- Failed to read File $file: $e")
        }
    }

    fun processCompoundTag(tag: CompoundTag): CompoundTag? {
        val updated = booleanArrayOf(false)
        tag.allKeys.forEach(Consumer { key: String ->
            val value: Tag = checkNotNull(tag.get(key))
            if (value.type === CompoundTag.TYPE) {
                val newTag = processCompoundTag(value as CompoundTag)
                if (newTag != null) {
                    updated[0] = true
                    tag.put(key, newTag)
                }
            } else if (value.type === ListTag.TYPE) {
                val newList = processListTag(value as ListTag)
                if (newList != null) {
                    updated[0] = true
                    tag.put(key, newList)
                }
            } else if (value.type === StringTag.TYPE) {
                val newString = modifyString(value.asString)
                if (newString != null) {
                    updated[0] = true
                    tag.putString(key, newString)
                }
            }
        })
        return if (updated[0]) tag else null
    }

    fun processListTag(listTag: ListTag): ListTag? {
        val updated = booleanArrayOf(false)
        val type = listTag.elementType
        val fn = when (type) {
            Tag.TAG_COMPOUND -> BiConsumer { i: Int, value: Tag ->
                val newTag = processCompoundTag((value as CompoundTag))
                if (newTag != null) {
                    updated[0] = true
                    listTag[i] = newTag
                }
            }

            Tag.TAG_LIST -> BiConsumer { i: Int, value: Tag ->
                val newList = processListTag((value as ListTag))
                if (newList != null) {
                    updated[0] = true
                    listTag[i] = newList
                }
            }

            Tag.TAG_STRING -> BiConsumer { i: Int, value: Tag ->
                val newString = modifyString(value.asString)
                if (newString != null) {
                    updated[0] = true
                    listTag[i] = StringTag.valueOf(newString)
                }
            }

            else -> BiConsumer { i: Int, value: Tag -> }
        }

        for (idx in listTag.indices) {
            val tag = listTag[idx]
            fn.accept(idx, tag)
        }
        return if (updated[0]) listTag else null
    }

    fun modifyString(input: String): String? {
        if (input.contains(":")) {
            val id = ResourceLocation.tryParse(input)
            if (id != null) {
                var namespace = id.namespace
                if (namespace == Transition.MINECRAFT) return null

                var path = id.path
                MappingsManager.ACTIVE_MAPPINGS.forEach { currentNamespace, mapping ->
                    if (id.namespace != currentNamespace && mapping.oldNamespaces.contains(id.namespace)) {
                        namespace = currentNamespace
                    }
                    if (namespace == currentNamespace && mapping.oldToNewPaths.containsKey(id.path)) {
                        path = mapping.oldToNewPaths[id.path]
                    }
                }
                val newId = ResourceLocation.fromNamespaceAndPath(namespace, path)
                if (isDev() && id != newId) {
                    Transition.log("    |- Old id: $id, New id: $newId")
                }
                return if (id !== newId) newId.toString() else null
            }
        }
        return null
    }
}
