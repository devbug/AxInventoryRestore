package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.type.ItemEnchantments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class SearchUtils {
    private static final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.builder().build();

    public static boolean matchesFilter(@NotNull String filter, @NotNull ItemStack item) {
        return matchesFilter(filter, WrappedItemStack.wrap(item));
    }

    public static boolean matchesFilter(@NotNull String filter, @NotNull WrappedItemStack wrapped) {
        filter = filter.toLowerCase(Locale.ENGLISH);

        for (ItemStack stack : StorageUtils.getStorageContents(wrapped, true)) {
            if (stack == null || stack.getType().isAir()) continue;
            WrappedItemStack wrap = WrappedItemStack.wrap(stack);
            boolean result = isItemMatching(filter, wrap);
            if (result) return true;
        }
        return false;
    }

    private static boolean isItemMatching(@NotNull String filter, @NotNull WrappedItemStack wrapped) {
        String material = wrapped.get(DataComponents.material()).toString();
        if (isMatching(filter, material)) return true;

        ItemEnchantments enchantments = wrapped.get(DataComponents.enchantments());
        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
                String enchantName = enchant.getKey().getKey() + " " + enchant.getValue();
                if (isMatching(filter, enchantName)) return true;
            }
        }

        ItemEnchantments storedEnchantments = wrapped.get(DataComponents.storedEnchantments());
        if (storedEnchantments != null) {
            for (Map.Entry<Enchantment, Integer> enchant : storedEnchantments.entrySet()) {
                String enchantName = enchant.getKey().getKey() + " " + enchant.getValue();
                if (isMatching(filter, enchantName)) return true;
            }
        }

        String customName = serializer.serializeOrNull(wrapped.get(DataComponents.customName()));
        if (isMatching(filter, customName)) return true;

        String itemName = serializer.serializeOrNull(wrapped.get(DataComponents.itemName()));
        if (isMatching(filter, itemName)) return true;

        for (Component line : wrapped.get(DataComponents.lore()).lines()) {
            String lore = serializer.serializeOrNull(line);
            if (isMatching(filter, lore)) return true;
        }

        return false;
    }

    private static boolean isMatching(@NotNull String filter, String original) {
        if (original == null) return false;
        original = original.toLowerCase();
        return original.contains(filter);
    }
}
