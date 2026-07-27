package com.artillexstudios.axinventoryrestore.backups;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class Backup {
    private final Map<String, List<BackupData>> deathsPerTypes = new ConcurrentHashMap<>();
    private final AtomicBoolean finished = new AtomicBoolean();

    public void addData(BackupData backupData) {
        if (CONFIG.getBoolean("enable-all-category", true)) {
            List<BackupData> list = deathsPerTypes.computeIfAbsent("ALL", reason -> new CopyOnWriteArrayList<>());
            list.add(backupData);
        }
        {
            List<BackupData> list = deathsPerTypes.computeIfAbsent(backupData.getReason(), reason -> new CopyOnWriteArrayList<>());
            list.add(backupData);
        }
    }

    public Map<String, List<BackupData>> getDeathsPerTypes() {
        return deathsPerTypes;
    }

    public List<BackupData> getDeathsByReason(@NotNull String saveReason) {
        return deathsPerTypes.get(saveReason);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public void finish() {
        this.finished.set(true);
    }
}
