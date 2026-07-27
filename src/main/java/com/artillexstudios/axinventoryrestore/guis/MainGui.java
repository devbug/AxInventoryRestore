package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import com.artillexstudios.axinventoryrestore.search.OpenDetails;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public class MainGui {
    private static final Set<MainGui> openedGuis = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final OpenDetails details;
    private final PaginatedGui mainGui;
    private final Player viewer;
    private final int rows = CONFIG.getInt("menu-rows.main-menu", 4);
    private final Backup backup;

    public MainGui(OpenDetails details, Player viewer) {
        this.details = details;
        this.viewer = viewer;
        this.backup = new Backup();

        mainGui = Gui.paginated()
                .title(StringUtils.format(LANG.getString("guis.maingui.title").replace("%player%", details.getName())))
                .rows(rows)
                .pageSize(rows * 9 - 9)
                .create();
    }

    public void open() {
        if (AxInventoryRestore.isDebugMode()) LogUtils.debug("Opening gui for {}", viewer.getName());
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            details.loadBackup(backup);
            update();
        }, Priority.HIGH);

        // Previous item
        mainGui.setItem(rows, 3, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.previous-page")).get(), event -> {
            mainGui.previous();
        }));
        // Next item
        mainGui.setItem(rows, 7, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.next-page")).get(), event -> {
            mainGui.next();
        }));

        mainGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        mainGui.setItem(rows, 5, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.close")).get(), event -> {
            mainGui.close(viewer);
        }));

        mainGui.open(viewer);
        openedGuis.add(this);
    }

    public void update() {
        mainGui.clearPageItems();
        Set<String> reasons = backup.getDeathsPerTypes().keySet();

        if (backup.isFinished() && ((CONFIG.getBoolean("enable-all-category") && reasons.size() == 1) || reasons.isEmpty())) {
            MESSAGEUTILS.sendLang(viewer, "errors.unknown-player", Map.of("%number%", "no backups found"));
            Scheduler.get().run(viewer, task -> {
                viewer.closeInventory();
            }, () -> {});
            return;
        }

        for (String saveReason : reasons) {
            ItemStack item = ItemBuilder.create(Material.PAPER).setName(StringUtils.formatToString("<!i>&#FFFF00&l" + saveReason)).get();
            List<BackupData> backupDataList = backup.getDeathsByReason(saveReason);
            if (backupDataList == null) continue;

            if (LANG.getSection("categories." + saveReason) != null) {
                item = ItemBuilder.create(LANG.getSection("categories." + saveReason), Map.of("%amount%", "" + backupDataList.size())).get();
            }

            mainGui.addItem(new GuiItem(item, event -> {
                new CategoryGui(details, viewer, backupDataList, mainGui, mainGui.getCurrentPageNum()).open();
            }));
        }
        mainGui.update();
    }

    public PaginatedGui getMainGui() {
        return mainGui;
    }

    public OpenDetails getDetails() {
        return details;
    }

    public Player getViewer() {
        return viewer;
    }

    public Backup getBackup() {
        return backup;
    }

    public static Set<MainGui> getOpenedGuis() {
        return openedGuis;
    }
}
