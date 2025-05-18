package org.neimeyer.lavaBin;

import org.bukkit.plugin.java.JavaPlugin;

public final class LavaBin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("LavaBin enabled, scheduling hopper‐check task…");
        // run hopper check every tick
        new HopperTask(this).runTaskTimer(this, 1, 1);
    }

    @Override
    public void onDisable() {
        getLogger().info("LavaBin disabled");
    }
}