package org.neimeyer.lavaBin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HopperTask extends BukkitRunnable {
  private final JavaPlugin plugin;

  // map each hopper‐location to how many ticks left before next delete
  private final Map<Location, Integer> cooldowns = new HashMap<>();

  /** 8 ticks is the vanilla hopper transfer cooldown */
  private static final int HOPPER_COOLDOWN = 8;

  public HopperTask(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    plugin.getLogger().info("[LavaBin] HopperTask tick");

    // track which hoppers we actually see this tick
    Set<Location> seen = new HashSet<>();

    for (World world : plugin.getServer().getWorlds()) {
      for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
        for (BlockState state : chunk.getTileEntities()) {
          if (!(state instanceof Hopper)) continue;

          Hopper hopper = (Hopper) state;
          Block hb = hopper.getBlock();
          Location loc = hb.getLocation();
          seen.add(loc);

          plugin.getLogger().info("[LavaBin]  ▶ Found Hopper @ " +
                  loc.getBlockX() + "," +
                  loc.getBlockY() + "," +
                  loc.getBlockZ());

          // decrement existing cooldown (if any)
          int cd = cooldowns.getOrDefault(loc, 0);
          if (cd > 0) {
            cooldowns.put(loc, cd - 1);
            continue;
          }

          // check if it’s pointing into a full lava cauldron
          org.bukkit.block.data.type.Hopper data =
                  (org.bukkit.block.data.type.Hopper) hb.getBlockData();
          BlockFace face = data.getFacing();
          Block target = hb.getRelative(face);
          if (target.getType() != Material.LAVA_CAULDRON) {
            // not the right target, leave cd at zero
            continue;
          }

          plugin.getLogger().info("[LavaBin]     ✓ Deleting 1 item from hopper");
          deleteOneItem(hopper);

          // reset this hopper’s cooldown
          cooldowns.put(loc, HOPPER_COOLDOWN);
        }
      }
    }

    // cleanup hoppers that have gone out of scope (unloaded etc)
    cooldowns.keySet().removeIf(k -> !seen.contains(k));
  }

  private void deleteOneItem(Hopper hopper) {
    Inventory inv = hopper.getInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (stack != null && stack.getType() != Material.AIR) {
        // remove a single item from that slot
        stack.setAmount(stack.getAmount() - 1);
        if (stack.getAmount() <= 0) {
          inv.setItem(i, null);
        } else {
          inv.setItem(i, stack);
        }
        break;
      }
    }
  }
}