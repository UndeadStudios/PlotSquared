package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused") public class PlayerEvents_1_8 extends PlotListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.isLeftClick() || (event.getAction() != InventoryAction.PLACE_ALL) || event
            .isShiftClick()) {
            return;
        }
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player) || !PlotSquared.get()
            .hasPlotArea(entity.getWorld().getName())) {
            return;
        }
        Player player = (Player) entity;
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();
        if ((slot > 8) || !event.getEventName().equals("InventoryCreativeEvent")) {
            return;
        }
        ItemStack current = inv.getItemInHand();
        ItemStack newItem = event.getCursor();
        ItemMeta newMeta = newItem.getItemMeta();
        ItemMeta oldMeta = newItem.getItemMeta();
        String newLore = "";
        if (newMeta != null) {
            List<String> lore = newMeta.getLore();
            if (lore != null) {
                newLore = lore.toString();
            }
        }
        String oldLore = "";
        if (oldMeta != null) {
            List<String> lore = oldMeta.getLore();
            if (lore != null) {
                oldLore = lore.toString();
            }
        }
        if (!"[(+NBT)]".equals(newLore) || (current.equals(newItem) && newLore.equals(oldLore))) {
            switch (newItem.getType()) {
                case LEGACY_BANNER:
                case PLAYER_HEAD:
                    if (newMeta != null)
                        break;
                default:
                    return;
            }
        }

        HashSet<Material> blocks = null;
        Block block = player.getTargetBlock(blocks, 7);
        BlockState state = block.getState();
        if (state == null) {
            return;
        }
        Material stateType = state.getType();
        Material itemType = newItem.getType();
        if (stateType != itemType) {
            switch (stateType) {
                case LEGACY_STANDING_BANNER:
                case LEGACY_WALL_BANNER:
                    if (itemType == Material.LEGACY_BANNER)
                        break;
                case LEGACY_SKULL:
                    if (itemType == Material.LEGACY_SKULL_ITEM)
                        break;
                default:
                    return;
            }
        }
        Location l = BukkitUtil.getLocation(state.getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlotAbs(l);
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        boolean cancelled = false;
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                cancelled = true;
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                cancelled = true;
            }
        } else {
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid)) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                    cancelled = true;
                }
            }
        }
        if (cancelled) {
            if ((current.getType() == newItem.getType()) && (current.getDurability() == newItem
                .getDurability())) {
                event.setCursor(
                    new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
                event.setCancelled(true);
                return;
            }
            event.setCursor(
                new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }

        EntityPortal_1_7_9.test(entity);

        Plot plot = area.getPlotAbs(l);
        PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                e.setCancelled(true);
            }
        } else {
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid)) {
                if (Flags.MISC_INTERACT.isTrue(plot)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                    e.setCancelled(true);
                }
            }
        }
    }
}
