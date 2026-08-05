package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class DynamicWorld {
    private final String name;

    public static DynamicWorld of(World world) {
        return new DynamicWorld(world.getName());
    }

    public static DynamicWorld of(String name) {
        return new DynamicWorld(name);
    }

    private DynamicWorld(String name) {
        this.name = name;
    }

    @Nullable
    public World get() {
        return Bukkit.getWorld(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DynamicWorld{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DynamicWorld world = (DynamicWorld) o;
        return name.equals(world.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
