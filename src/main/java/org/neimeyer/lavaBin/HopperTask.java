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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HopperTask extends BukkitRunnable {
  private final JavaPlugin plugin;
  private final Map<Location, Integer> cooldowns = new HashMap<>();

  private static final int HOPPER_COOLDOWN = 8;

  public HopperTask(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    Set<Location> seen = new HashSet<>();

    for (World world : plugin.getServer().getWorlds()) {
      for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
        for (BlockState state : chunk.getTileEntities()) {
          if (!(state instanceof Hopper)) continue;

          Hopper hopper = (Hopper) state;
          Location loc = hopper.getBlock().getLocation();
          seen.add(loc);

          int cd = cooldowns.getOrDefault(loc, 0);
          if (cd > 0) {
            cooldowns.put(loc, cd - 1);
            continue;
          }

          org.bukkit.block.data.type.Hopper data =
                  (org.bukkit.block.data.type.Hopper) hopper.getBlock().getBlockData();
          BlockFace face = data.getFacing();
          Block target = hopper.getBlock().getRelative(face);
          if (target.getType() != Material.LAVA_CAULDRON) {
            continue;
          }

          deleteOneItem(hopper);
          cooldowns.put(loc, HOPPER_COOLDOWN);
        }
      }
    }

    cooldowns.keySet().removeIf(k -> !seen.contains(k));
  }

  private void deleteOneItem(Hopper hopper) {
    Inventory inv = hopper.getInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (stack != null && stack.getType() != Material.AIR) {
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