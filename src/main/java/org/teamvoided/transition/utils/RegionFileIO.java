package org.teamvoided.transition.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

interface RegionFileIO {
    static RegionFile getRegionFile(Path file) throws IOException {
        RegionFile regionFile2 = new RegionFile(new RegionStorageInfo("", Level.OVERWORLD, ""), file, file.getParent(), false);
        return regionFile2;
    }

    @Nullable
    static CompoundTag read(File file) throws IOException {
        RegionFile regionFile = getRegionFile(file.toPath());
        DataInputStream dataInputStream = regionFile.getChunkDataInputStream(new ChunkPos(0, 0));

        CompoundTag tag;
        label43:
        {
            try {
                if (dataInputStream == null) {
                    tag = null;
                    break label43;
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