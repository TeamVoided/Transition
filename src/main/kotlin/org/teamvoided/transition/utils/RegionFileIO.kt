package org.teamvoided.transition.utils

import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import org.teamvoided.transition.Transition
import org.teamvoided.transition.Transition.MODID
import org.teamvoided.transition.Transition.id
import java.io.File
import java.io.IOException
import java.nio.file.Path

object RegionFileIO {
    const val REGION_CHUNKS: Int = 32
    val fakeDimension: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, id("none"))
    val fakeData = RegionStorageInfo("${MODID}_world", fakeDimension, MODID)

    @Throws(IOException::class)
    fun getRegionFile(file: Path): RegionFile = RegionFile(fakeData, file, file.parent, false)

    @Throws(IOException::class)
    fun read(file: File): HashMap<ChunkPos, CompoundTag> {
        val regionFile = getRegionFile(file.toPath())

        // r.0.0.mca
        val cordArray = file.getName().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }

        val x = cordArray[1].toInt()
        val z = cordArray[2].toInt()

        val map = HashMap<ChunkPos, CompoundTag>()
        for (i in 0..<REGION_CHUNKS) {
            for (j in 0..<REGION_CHUNKS) {
                val chunkPos = ChunkPos((x * REGION_CHUNKS) + i, (z * REGION_CHUNKS) + j)
                if (regionFile.doesChunkExist(chunkPos)) {
                    try {
                        val nbt = read(regionFile, chunkPos)
                            ?: throw NullPointerException("Data input stream was null in region: ${file.name}, chunk: $chunkPos")
                        map.put(chunkPos, nbt)
                    } catch (e: Throwable) {
                        Transition.error("Failed to read chunk: $chunkPos in region: ${file.name}")
                        Transition.error(e.message)
                        Transition.error(e.stackTraceToString())
                    }
                }
            }
        }
        return map
    }

    @Throws(IOException::class)
    fun read(regionFile: RegionFile, chunkPos: ChunkPos): CompoundTag? {
        val dataInputStream = regionFile.getChunkDataInputStream(chunkPos) ?: return null
        var tag: CompoundTag
        try {
            tag = NbtIo.read(dataInputStream)
        } catch (var7: Throwable) {
            try {
                dataInputStream.close()
            } catch (var6: Throwable) {
                var7.addSuppressed(var6)
            }
            throw var7
        }
        try {
            dataInputStream.close()
        } catch (e: Throwable) {
            throw e
        }
        return tag
    }

    @Throws(IOException::class)
    fun write(file: File, chunkPos: ChunkPos, compoundTag: CompoundTag) {
        write(getRegionFile(file.toPath()), chunkPos, compoundTag)
    }

    @Throws(IOException::class)
    fun write(regionFile: RegionFile, chunkPos: ChunkPos, compoundTag: CompoundTag) {
        val dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos)
        try {
            NbtIo.write(compoundTag, dataOutputStream)
        } catch (var8: Throwable) {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close()
                } catch (var7: Throwable) {
                    var8.addSuppressed(var7)
                }
            }

            throw var8
        }
        dataOutputStream.close()
    }
}