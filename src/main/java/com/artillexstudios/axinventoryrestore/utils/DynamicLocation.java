package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DynamicLocation {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final DynamicWorld dynamicWorld;

    public static DynamicLocation of(Location l) {
        return of(l, DynamicWorld.of(l.getWorld()));
    }

    public static DynamicLocation of(Block b) {
        return of(b.getLocation(), DynamicWorld.of(b.getWorld()));
    }

    public static DynamicLocation of(Location l, DynamicWorld dynamicWorld) {
        return of(dynamicWorld, l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static DynamicLocation of(DynamicWorld dynamicWorld, double x, double y, double z) {
        return of(dynamicWorld, x, y, z, 0, 0);
    }

    public static DynamicLocation of(DynamicWorld dynamicWorld, double x, double y, double z, float yaw, float pitch) {
        return new DynamicLocation(dynamicWorld, x, y, z, yaw, pitch);
    }

    public static DynamicLocation deserialize(String val) {
        String[] s = val.trim().split(";");
        return of(DynamicWorld.of(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]), Double.parseDouble(s[3]), Float.parseFloat(s[4]), Float.parseFloat(s[5]));
    }

    private DynamicLocation(DynamicWorld dynamicWorld, double x, double y, double z) {
        this(dynamicWorld, x, y, z, 0, 0);
    }

    private DynamicLocation(DynamicWorld dynamicWorld, double x, double y, double z, float yaw, float pitch) {
        this.dynamicWorld = dynamicWorld;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return Location.locToBlock(x);
    }

    public int getBlockY() {
        return Location.locToBlock(y);
    }

    public int getBlockZ() {
        return Location.locToBlock(z);
    }

    public String getReadable() {
        String world = dynamicWorld.getName();
        return String.format("%s: %.1f, %.1f, %.1f", world, x, y, z);
    }

    public DynamicLocation clone() {
        return new DynamicLocation(dynamicWorld, x, y, z, yaw, pitch);
    }

    @Nullable
    public Location get() {
        World world = dynamicWorld.get();
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    public DynamicLocation add(double x, double y, double z) {
        return new DynamicLocation(dynamicWorld, this.x + x, this.y + y, this.z + z);
    }

    @NotNull
    public DynamicLocation add(DynamicLocation location) {
        return new DynamicLocation(dynamicWorld, x + location.x, y + location.y, z + location.z);
    }

    @NotNull
    public DynamicLocation subtract(double x, double y, double z) {
        return new DynamicLocation(dynamicWorld, this.x - x, this.y - y, this.z - z);
    }

    @NotNull
    public DynamicLocation subtract(DynamicLocation location) {
        return new DynamicLocation(dynamicWorld, x - location.x, y - location.y, z - location.z);
    }

    public double distance(DynamicLocation location) {
        return Math.sqrt(distanceSquared(location));
    }

    public double distanceSquared(DynamicLocation location) {
        if (!location.getWorld().equals(dynamicWorld)) {
            throw new IllegalArgumentException("Cannot measure distance between " + dynamicWorld.getName() + " and " + location.getWorld().getName());
        }
        return NumberConversions.square(x - location.x) + NumberConversions.square(y - location.y) + NumberConversions.square(z - location.z);
    }

    public DynamicWorld getWorld() {
        return dynamicWorld;
    }

    @Override
    public String toString() {
        return "DynamicLocation{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", dynamicWorld=" + dynamicWorld +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DynamicLocation location = (DynamicLocation) o;
        return Double.compare(x, location.x) == 0 && Double.compare(y, location.y) == 0 && Double.compare(z, location.z) == 0 && Float.compare(yaw, location.yaw) == 0 && Float.compare(pitch, location.pitch) == 0 && Objects.equals(dynamicWorld, location.dynamicWorld);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Float.hashCode(yaw);
        result = 31 * result + Float.hashCode(pitch);
        result = 31 * result + Objects.hashCode(dynamicWorld);
        return result;
    }
}
