package org.teamvoided.transition.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import static org.teamvoided.transition.Transition.LOGGER;
import static org.teamvoided.transition.Transition.log;

public interface RegionFileIO {
    RegionStorageInfo fakeData = new RegionStorageInfo("", Level.OVERWORLD, "");
    static RegionFile getRegionFile(Path file) throws IOException {
        return new RegionFile(fakeData, file, file.getParent(), false);
    }

    int REGION_CHUNKS = 32;

    static HashMap<ChunkPos, CompoundTag> read(File file) throws IOException {
        RegionFile regionFile = getRegionFile(file.toPath());

        // r.0.0.mca
        var cordArray = file.getName().split("\\.");

        var x = Integer.parseInt(cordArray[1]);
        var z = Integer.parseInt(cordArray[2]);

        var map = new HashMap<ChunkPos, CompoundTag>();
                LOGGER.error("REGION FILE READER IS STILL WRONG!!!");
        for (int i = 0; i < REGION_CHUNKS; i++) {
            for (int j = 0; j < REGION_CHUNKS; j++) {
                var chunkPos = new ChunkPos((x * REGION_CHUNKS) + i, (z * REGION_CHUNKS) + j);
                if (regionFile.doesChunkExist(chunkPos)) map.put(chunkPos, read(regionFile, chunkPos));
            }
        }
        return map;

    }

    static CompoundTag read(RegionFile regionFile, ChunkPos chunkPos) throws IOException {
        DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);
        CompoundTag tag;
        funny:
        {
            try {
                if (dataInputStream == null) {
                    tag = null;
                    break funny;
                }

                tag = NbtIo.read(dataInputStream);
            } catch (Throwable var7) {
                try {
                    dataInputStream.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }

                throw var7;
            }
            dataInputStream.close();
            return tag;
        }

        if (dataInputStream != null) {
            dataInputStream.close();
        }

        return tag;
    }

    static void write(File file, ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        write(getRegionFile(file.toPath()), chunkPos, compoundTag);
    }

    static void write(RegionFile regionFile, ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        DataOutputStream dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos);
        try {
            NbtIo.write(compoundTag, dataOutputStream);
        } catch (Throwable var8) {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }
            }

            throw var8;
        }
        dataOutputStream.close();
    }
}