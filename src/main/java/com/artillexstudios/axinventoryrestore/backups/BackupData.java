package com.artillexstudios.axinventoryrestore.backups;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.hooks.AxShulkersHook;
import com.artillexstudios.axinventoryrestore.hooks.HookManager;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import com.artillexstudios.axinventoryrestore.utils.DateUtils;
import com.artillexstudios.axinventoryrestore.utils.DynamicLocation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;

public class BackupData {
    private final int id;
    private final UUID player;
    private final String reason;
    private final DynamicLocation location;
    private final long date;
    private final String cause;
    private final int inventoryId;
    private CompletableFuture<ItemStack[]> future = null;
    private ItemStack[] rawItems = null;
    private final int expLevel;
    private final float expProgress;
    private volatile ItemStack[] items = null;

    public BackupData(int id, @NotNull UUID player, @NotNull String reason, @NotNull DynamicLocation location, long date, String cause, int inventoryId, int expLevel, float expProgress) {
        this.id = id;
        this.player = player;
        this.reason = reason;
        this.location = location;
        this.date = date;
        this.cause = cause;
        this.inventoryId = inventoryId;
        this.expLevel = expLevel;
        this.expProgress = expProgress;
    }

    public int getId() {
        return id;
    }

    public DynamicLocation getLocation() {
        return location;
    }

    public CompletableFuture<ItemStack[]> getItems() {
        if (this.items != null) {
            return CompletableFuture.completedFuture(this.items);
        }

        if (future != null) return future;
        future = new CompletableFuture<>();
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            ItemStack[] items = rawItems != null ? rawItems : AxInventoryRestore.getDatabase().getItemsFromBackup(inventoryId);
            AxShulkersHook hook = HookManager.getAxShulkersHook();
            if (hook == null) {
                future.complete(items);
                return;
            }

            List<CompletableFuture<ItemStack>> futures = new ArrayList<>();
            for (ItemStack item : items) {
                if (!hook.isShulker(item)) continue;
                CompletableFuture<ItemStack> itemFuture = new CompletableFuture<>();
                Scheduler.get().run(task -> {
                    hook.clean(item);
                    itemFuture.complete(item);
                });
                futures.add(itemFuture);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                this.items = items;
                future.complete(items);
            });
        }, Priority.HIGH);
        return future;
    }

    public void setItems(ItemStack[] items) {
        this.rawItems = items;
    }

    public long getDate() {
        return date;
    }

    public UUID getPlayerUUID() {
        return player;
    }

    public String getPlayerName() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        return Optional.ofNullable(offlinePlayer.getName()).orElse(player.toString());
    }

    public String getReason() {
        return reason;
    }

    public String getCause() {
        return cause;
    }

    public int getExpLevel() {
        return expLevel;
    }

    public float getExpProgress() {
        return expProgress;
    }

    public String getExpProgressFormatted() {
        return String.format("%.1f%%", expProgress * 100);
    }

    public CompletableFuture<ArrayList<ItemStack>> getInShulkers(@NotNull String restorerName) {
        return getItems().thenApply(items -> {
            ArrayList<ItemStack> shulkerItems = new ArrayList<>();
            List<ItemStack> itemsCopy = new ArrayList<>(Arrays.asList(items));

            while (!itemsCopy.isEmpty()) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("%date%", DateUtils.formatDate(date));
                replacements.put("%location%", location.getReadable());
                replacements.put("%cause%", cause == null ? "---" : cause);
                replacements.put("%staff%", restorerName);
                replacements.put("%player-uuid%", player.toString());
                replacements.put("%level%", String.valueOf(expLevel));

                ItemStack shulkerIt = ItemBuilder.create(LANG.getSection("restored-shulker"), replacements).get();
                BlockStateMeta im = (BlockStateMeta) shulkerIt.getItemMeta();
                ShulkerBox shulker = (ShulkerBox) im.getBlockState();

                final Iterator<ItemStack> iterator = itemsCopy.iterator();
                while (iterator.hasNext()) {
                    ItemStack it = iterator.next();
                    if (it == null) {
                        iterator.remove();
                        continue;
                    }
                    if (shulker.getInventory().firstEmpty() == -1) break;

                    if (Tag.SHULKER_BOXES.isTagged(it.getType())) {
                        shulkerItems.add(it);
                    } else {
                        shulker.getInventory().addItem(it);
                    }
                    iterator.remove();
                }

                im.setBlockState(shulker);
                shulkerIt.setItemMeta(im);
                shulkerItems.add(shulkerIt);
            }

            return shulkerItems;
        });
    }
}
