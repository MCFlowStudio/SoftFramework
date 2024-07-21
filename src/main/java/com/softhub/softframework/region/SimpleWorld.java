package com.softhub.softframework.region;

import lombok.NonNull;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.List;

public interface SimpleWorld {

    boolean getUseWorldborder();

    int getCenterX();

    int getCenterZ();

    int getMaxRadius();

    int getMinRadius();

    List<String> getBiomes();

    @NonNull World getWorld();

    int getMinY();

    int getMaxY();

    @Nullable
    default String getID() {
        return null;
    }

}
