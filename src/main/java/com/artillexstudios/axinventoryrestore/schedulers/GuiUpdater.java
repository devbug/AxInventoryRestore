package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {
    private static ExceptionReportingScheduledThreadPool pool = null;

    public static void start() {
        if (pool != null) pool.shutdown();

        pool = new ExceptionReportingScheduledThreadPool(1);

        pool.scheduleAtFixedRate(() -> {
            for (Iterator<MainGui> it = MainGui.getOpenedGuis().iterator(); it.hasNext(); ) {
                MainGui mainGui = it.next();
                Inventory topInventory = mainGui.getViewer().getOpenInventory().getTopInventory();
                if (topInventory.getType() == InventoryType.CHEST) { // keep gui updating while browsing
                    if (topInventory.equals(mainGui.getMainGui().getInventory())) mainGui.update();
                    continue;
                }
                mainGui.getBackup().finish();
                it.remove();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        if (pool == null) return;
        pool.shutdown();
    }
}
