package com.softhub.softframework.region;

import lombok.NonNull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;

public interface SimpleChunk {

    @NonNull
    SimpleWorld getSimpleWorld();

    int getX();

    int getZ();

    List<Entity> getEntities();

    boolean isLoaded();

    void saveChunk();

    @Nullable
    BlockData[][][] getBlockData();

    @Nullable
    default String getID() {
        return null;
    }
}
