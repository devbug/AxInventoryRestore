package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.search.OpenDetails;
import org.bukkit.entity.Player;

public enum Search {
    INSTANCE;

    public void execute(Player sender, String search) {
        Scheduler.get().execute(sender, () -> {
            new MainGui(
                    OpenDetails.search(search),
                    sender
            ).open();
        }, () -> {}, 0);
    }
}
