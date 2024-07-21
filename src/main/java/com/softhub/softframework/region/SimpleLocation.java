package com.softhub.softframework.region;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

public interface SimpleLocation {

    @NonNull
    SimpleWorld getSimpleWorld();

    double getX();

    double getY();

    double getZ();

    float getPitch();

    float getYaw();

    <T extends Entity> T spawn(Class<T> entityClass);

    default Location getLocation() {
        return new Location(getSimpleWorld().getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @Nullable
    default String getID() {
        return null;  // UUID or similar identifier for the location
    }
}